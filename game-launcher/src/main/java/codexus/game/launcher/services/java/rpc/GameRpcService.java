package codexus.game.launcher.services.java.rpc;

import codexus.cipher.entities.*;
import codexus.cipher.entities.wpflauncher.netgame.*;
import codexus.cipher.entities.wpflauncher.netgame.texture.*;
import codexus.cipher.entities.wpflauncher.rpc.*;
import codexus.cipher.skip32.Skip32Cipher;
import codexus.cipher.utils.http.HttpWrapper;
import codexus.csharpsup.DotNetEncoding;
import codexus.development.sdk.manager.IUserManager;
import codexus.game.launcher.managers.TaskManager;
import codexus.game.launcher.services.java.rpc.events.*;
import codexus.game.launcher.utils.FileUtil;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public final class GameRpcService {
    @FunctionalInterface public interface GetComponents {
        Entity<EntityComponentDownloadInfoResponse> get(String userId, String userToken, String id);
    }
    @FunctionalInterface public interface GetSkinList {
        List<EntityUserGameTexture> get(String userId, String userToken, EntityUserGameTextureRequest request);
    }
    private static final InetAddress LOOPBACK = InetAddress.ofLiteral("127.0.0.1");
    private static final System.Logger LOG = System.getLogger(GameRpcService.class.getName());
    private final int port;
    private final String serverIp, serverPort, roleName, userId, userToken;
    private final EnumGameVersion gameVersion;
    private final HttpWrapper http = new HttpWrapper();
    private final SocketCallback callbacks = new SocketCallback();
    private final List<byte[]> sendCache = new ArrayList<>();
    private final Skip32Cipher skip32 = new Skip32Cipher(DotNetEncoding.utf8("SaintSteve"));
    private String skinPath = "";
    private GetComponents getComponents;
    private GetSkinList getSkin;
    private final Object sendLock = new Object();
    private volatile boolean launchIndexReady;
    private volatile boolean normalExit;
    private ServerSocket listener;
    private volatile InputStream reader;
    private volatile OutputStream writer;
    public volatile Socket Client;
    public GameRpcService(int port, String serverIp, String serverPort, String roleName, String userId,
                          String userToken, EnumGameVersion gameVersion) {
        this.port = port; this.serverIp = serverIp; this.serverPort = serverPort; this.roleName = roleName;
        this.userId = userId; this.userToken = userToken; this.gameVersion = gameVersion;
    }
    public void connect(String skinPath, GetSkinList getSkinListFunc, GetComponents getComponentsFunc) {
        this.skinPath = skinPath; getSkin = getSkinListFunc; getComponents = getComponentsFunc;
        callbacks.registerReceiveCallback(18, this::onCheckPlayerMsg);
        callbacks.registerReceiveCallback(19, data -> LOG.log(System.Logger.Level.INFO, "[RPC] PCyc Entity [] sent"));
        callbacks.registerReceiveCallback(261, this::handleAuthenticationNewVersion);
        callbacks.registerReceiveCallback(512, data -> sendControlData(SimplePack.pack((short) 512, "i'am wpflauncher")));
        callbacks.registerReceiveCallback(517, data -> sendControlData(SimplePack.pack((short) 1031, serverIp, Integer.parseInt(serverPort.strip()), roleName, false)));
        callbacks.registerReceiveCallback(520, this::handlePlayerSkin);
        callbacks.registerReceiveCallback(1298, data -> sendControlData(SimplePack.pack((short) 1298, false, 0L, 0L)));
        callbacks.registerReceiveCallback(4612, data -> LOG.log(System.Logger.Level.INFO, "[RPC] Login Game"));
        normalExit = false;
        startControlConnection(2);
    }
    private void startControlConnection(int tries) {
        try {
            listener = new ServerSocket(); listener.bind(new InetSocketAddress(LOOPBACK, port));
            Thread.ofPlatform().name("codexus-rpc-listener").start(this::listenControlConnect);
            LOG.log(System.Logger.Level.INFO, "[RPC] Control connection started on port " + port);
        } catch (Exception e) {
            if (tries > 0) startControlConnection(tries - 1); else closeControlConnection();
            LOG.log(System.Logger.Level.ERROR, "[RPC] Failed to start control connection after retries", e);
        }
    }
    public void closeControlConnection() {
        normalExit = true;
        ServerSocket server = listener;
        listener = null;
        try {
            if (server != null) server.close();
        } catch (IOException e) { LOG.log(System.Logger.Level.WARNING, "[RPC] Failed to close listener", e); }
        synchronized (sendLock) {
            Socket client = Client;
            Client = null; reader = null; writer = null;
            try { if (client != null) client.close(); }
            catch (IOException e) { LOG.log(System.Logger.Level.WARNING, "[RPC] Failed to close client", e); }
            sendCache.clear();
        }
        http.close();
        closeGameCleaning();
    }
    private void handleAuthenticationNewVersion(byte[] data) {
        if (gameVersion != null && gameVersion.value() > EnumGameVersion.V_1_18.value())
            sendControlData(SimplePack.pack((short) 1799, serverIp, Integer.parseInt(serverPort.strip()), roleName));
    }
    private void handlePlayerSkin(byte[] content) {
        var message = new SimpleUnpack(content).unpack(new EntityOtherEnterWorldMsg());
        CompletableFuture.runAsync(() -> processPlayerSkin(message), TaskManager.Instance.getFactory());
    }
    private void processPlayerSkin(EntityOtherEnterWorldMsg message) {
        var request = new EntityUserGameTextureRequest();
        request.UserId = Long.toString(skip32.computeUserIdFromUuid(message.Uuid));
        request.setClientType(EnumGameClientType.Java);
        List<EntityUserGameTexture> skins = getSkin == null ? null : getSkin.get(userId, userToken, request);
        String filePath = "";
        int skinMode = 0;
        if (skins != null) {
            for (EntityUserGameTexture skin : skins) {
                if (skin.SkinId.length() <= 5) continue;
                skinMode = skin.SkinMode;
                Path temporary = Path.of(skinPath, "skin_" + skin.SkinId + ".png");
                if (Files.isRegularFile(temporary) && FileUtil.isFileReadable(temporary.toString())) { filePath = temporary.toString(); break; }
                try {
                    var manager = IUserManager.Current.Instance;
                    var user = manager == null ? null : manager.getAvailableUser(userId);
                    if (user == null) continue;
                    var response = getComponents == null ? null : getComponents.get(userId, user.AccessToken, skin.SkinId);
                    if (response != null && response.Code != 0) continue;
                    String url = response == null || response.Data == null || response.Data.SubEntities.isEmpty()
                            ? null : response.Data.SubEntities.getFirst().ResUrl;
                    if (url != null) {
                        Files.createDirectories(Path.of(skinPath));
                        var download = http.getAsync(url).join(); HttpWrapper.ensureSuccess(download);
                        FileUtil.writeFileSafelyAsync(temporary.toString(), download.body()).join();
                        if (Files.isRegularFile(temporary) && FileUtil.isFileReadable(temporary.toString())) { filePath = temporary.toString(); break; }
                    }
                } catch (Exception e) {
                    LOG.log(System.Logger.Level.ERROR, "[RPC] Failed to handle skin for player " + message.Name, e);
                    try { Files.deleteIfExists(temporary); }
                    catch (Exception cleanup) { LOG.log(System.Logger.Level.ERROR, "[RPC] Failed to delete temp file " + temporary, e); }
                }
            }
        }
        sendControlData(SimplePack.pack((short) 520, message.Name, filePath, "", skinMode));
    }
    private void onCheckPlayerMsg(byte[] data) {
        var content = new EntityCheckPlayerMessage(); content.Length = 0; content.Message = "";
        new SimpleUnpack(data).unpack(content);
        sendControlData(SimplePack.pack((short) 18, content.Length, content.Message));
    }
    private void listenControlConnect() {
        try {
            ServerSocket server = listener;
            while (!normalExit && server != null) {
                Socket client = server.accept();
                if (!client.getInetAddress().equals(LOOPBACK)) { client.close(); continue; }
                synchronized (sendLock) {
                    Socket previous = Client;
                    Client = client;
                    if (previous != null) previous.close();
                    reader = client.getInputStream(); writer = client.getOutputStream();
                    sendCacheControlData();
                }
                Thread.ofPlatform().name("codexus-rpc-receiver").start(() -> onRecvControlData(client));
            }
        } catch (Exception e) {
            if (!normalExit) LOG.log(System.Logger.Level.ERROR, "[RPC] Failed to listen control connection", e);
        }
    }
    private void sendControlData(byte[] message) {
        if (message == null) return;
        synchronized (sendLock) {
            if (normalExit) return;
            OutputStream target = writer;
            if (target == null) { sendCache.add(message); return; }
            byte[] buffer = new byte[message.length + 2];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).putShort((short) message.length);
            System.arraycopy(message, 0, buffer, 2, message.length);
            try { target.write(buffer); target.flush(); }
            catch (Exception e) { LOG.log(System.Logger.Level.ERROR, "[RPC] Failed to send control data", e); }
        }
    }
    private void onRecvControlData(Socket client) {
        try (client) {
            InputStream source = client.getInputStream();
            while (!normalExit && Client == client) {
                int low = source.read(), high = source.read();
                if (low < 0 || high < 0) throw new EOFException();
                int length = low | high << 8;
                byte[] message = source.readNBytes(length);
                if (message.length != length) throw new EOFException("Truncated RPC packet");
                handleMcControlMessage(message);
            }
        } catch (EOFException ignored) {
        } catch (Exception e) {
            if (!normalExit && Client == client) LOG.log(System.Logger.Level.ERROR, "[RPC] Error receiving control data", e);
        } finally {
            synchronized (sendLock) {
                if (Client == client) { Client = null; reader = null; writer = null; closeGameCleaning(); }
            }
        }
    }
    private void handleMcControlMessage(byte[] message) {
        int id = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getShort() & 65535;
        if (!launchIndexReady && id == 261) launchIndexReady = true;
        callbacks.invokeCallback(id, Arrays.copyOfRange(message, 2, message.length));
    }
    private void closeGameCleaning() { launchIndexReady = false; }
    private void sendCacheControlData() { for (byte[] message : sendCache) sendControlData(message); sendCache.clear(); }
}
