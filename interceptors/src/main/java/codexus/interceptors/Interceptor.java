package codexus.interceptors;

import codexus.development.sdk.analysis.*;
import codexus.development.sdk.entities.*;
import codexus.development.sdk.event.EventCreateInterceptor;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.utils.*;
import codexus.interceptors.handlers.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class Interceptor implements AutoCloseable {
    private static final ConcurrentMap<UUID, InterceptorConfig> CONFIGS = new ConcurrentHashMap<>();
    private final EventLoopGroup acceptorGroup, workerGroup;
    private final Object configLock = new Object();
    private Channel channel;
    private UdpBroadcaster udpBroadcaster;
    public final ConcurrentMap<ChannelId, Channel> ActiveChannels = new ConcurrentHashMap<>();
    public final UUID Identifier = UUID.randomUUID();
    private volatile InterceptorConfig currentConfig;
    public InterceptorConfig getCurrentConfig() { return currentConfig; }
    public boolean isRental() { return currentConfig.IsRental; }
    public String getLocalAddress() { return currentConfig.LocalAddress; }
    public int getLocalPort() { return currentConfig.LocalPort; }
    public String getNickName() { return currentConfig.NickName; }
    public String getForwardAddress() { return currentConfig.ForwardAddress; }
    public int getForwardPort() { return currentConfig.ForwardPort; }
    public String getServerName() { return currentConfig.ServerName; }
    public String getServerVersion() { return currentConfig.ServerVersion; }
    public Interceptor(EventLoopGroup acceptorGroup, EventLoopGroup workerGroup) {
        this.acceptorGroup = acceptorGroup;
        this.workerGroup = workerGroup;
        currentConfig = new InterceptorConfig();
        currentConfig.IsRental = false;
        currentConfig.LocalAddress = "127.0.0.1";
        currentConfig.LocalPort = 0;
        currentConfig.NickName = currentConfig.ForwardAddress = currentConfig.ServerName = currentConfig.ServerVersion = "";
        currentConfig.ModInfo = currentConfig.GameId = currentConfig.UserId = currentConfig.UserToken = "";
        currentConfig.ForwardPort = 25565;
    }
    public Interceptor(EventLoopGroup acceptorGroup, EventLoopGroup workerGroup, String serverName,
                       String serverVersion, String forwardAddress, int forwardPort, String localAddress,
                       int localPort, String nickName) {
        this(acceptorGroup, workerGroup);
        currentConfig.ServerName = serverName;
        currentConfig.ServerVersion = serverVersion;
        currentConfig.ForwardAddress = forwardAddress;
        currentConfig.ForwardPort = forwardPort;
        currentConfig.LocalAddress = localAddress;
        currentConfig.LocalPort = localPort;
        currentConfig.NickName = nickName;
    }
    public static Interceptor createInterceptor(boolean isRental, EntitySocks5 socks5, String modInfo,
            String gameId, String serverName, String serverVersion, String forwardAddress, int forwardPort,
            String nickName, String userId, String userToken, BiConsumer<InterceptorConfig, String> onJoinServer,
            String localAddress, int localPort) {
        var event = EventManager.Instance.triggerEvent("channel_interceptor", new EventCreateInterceptor(localPort));
        if (event.IsCancelled) throw new IllegalStateException("Create Interceptor cancelled");
        if (forwardPort <= 0) forwardPort = 25565;
        int port = NetworkUtil.getAvailablePort(event.Port, 35565, true);
        var acceptor = new NioEventLoopGroup();
        var worker = new NioEventLoopGroup();
        var result = new Interceptor(acceptor, worker, serverName, serverVersion, forwardAddress, forwardPort,
                localAddress, port, nickName);
        InterceptorConfig config = result.currentConfig;
        config.IsRental = isRental;
        config.ModInfo = modInfo;
        config.GameId = gameId;
        config.UserId = userId;
        config.UserToken = userToken;
        config.OnJoinServer = onJoinServer;
        config.Socks5 = socks5;
        CONFIGS.put(result.Identifier, config);
        var bootstrap = new ServerBootstrap().group(acceptor, worker).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_RCVBUF, 1048576)
                .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_SNDBUF, 1048576).childOption(ChannelOption.SO_RCVBUF, 1048576)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(524288, 1048576))
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override protected void initChannel(Channel channel) {
                        InterceptorConfig current = CONFIGS.getOrDefault(result.Identifier, result.currentConfig);
                        channel.pipeline().addLast("splitter", new MessageDeserializer21Bit())
                                .addLast("handler", new ServerHandler(result, current))
                                .addLast("pre-encoder", new MessageSerializer21Bit())
                                .addLast("encoder", new MessageSerializer());
                    }
                });
        try {
            result.channel = bootstrap.bind(new InetSocketAddress("0.0.0.0", port)).syncUninterruptibly().channel();
        } catch (RuntimeException e) {
            CONFIGS.remove(result.Identifier);
            acceptor.shutdownGracefully();
            worker.shutdownGracefully();
            throw e;
        }
        System.getLogger(Interceptor.class.getName()).log(System.Logger.Level.INFO,
                "Listening on port {0}, forward to {1}:{2}, nickname {3}", port, forwardAddress, forwardPort, nickName);
        result.udpBroadcaster = result.createUdpBroadcaster();
        result.udpBroadcaster.startBroadcastingAsync();
        return result;
    }
    public static Interceptor createInterceptor(boolean isRental, EntitySocks5 socks5, String modInfo,
            String gameId, String serverName, String serverVersion, String forwardAddress, int forwardPort,
            String nickName, String userId, String userToken, BiConsumer<InterceptorConfig, String> onJoinServer) {
        return createInterceptor(isRental, socks5, modInfo, gameId, serverName, serverVersion, forwardAddress,
                forwardPort, nickName, userId, userToken, onJoinServer, "127.0.0.1", 6445);
    }
    public static Interceptor createInterceptor(EntitySocks5 socks5, String modInfo, String gameId,
            String serverName, String serverVersion, String forwardAddress, int forwardPort, String nickName,
            String userId, String userToken, Consumer<String> onJoinServer, String localAddress, int localPort) {
        return createInterceptor(false, socks5, modInfo, gameId, serverName, serverVersion, forwardAddress,
                forwardPort, nickName, userId, userToken,
                onJoinServer == null ? null : (config, hash) -> onJoinServer.accept(hash), localAddress, localPort);
    }
    public void updateConfig(InterceptorConfig newConfig) {
        synchronized (configLock) {
            currentConfig = newConfig;
            CONFIGS.put(Identifier, newConfig);
        }
    }
    public void changeServer(String newGameName, String newGameId, String newGameVersion, String newModInfo,
                             String newAddress, int newPort) {
        var config = currentConfig.clone();
        config.ServerName = newGameName;
        config.GameId = newGameId;
        config.ServerVersion = newGameVersion;
        config.ModInfo = newModInfo;
        config.ForwardAddress = newAddress;
        config.ForwardPort = newPort;
        updateConfig(config);
        restartUdpBroadcaster();
    }
    public void changeRole(String newRoleName, String newUserId, String newUserToken) {
        var config = currentConfig.clone();
        config.NickName = newRoleName;
        config.UserId = newUserId;
        config.UserToken = newUserToken;
        updateConfig(config);
        restartUdpBroadcaster();
    }
    public void shutdownAsync() {
        try {
            if (udpBroadcaster != null) { udpBroadcaster.close(); udpBroadcaster = null; }
            for (Channel client : ActiveChannels.values()) {
                var connection = client.attr(ChannelAttribute.Connection).getAndSet(null);
                connection.shutdown();
            }
            if (channel != null) channel.close();
            CONFIGS.remove(Identifier);
            acceptorGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Failed to shut down interceptor " + Identifier, e);
        }
    }
    @Override public void close() { shutdownAsync(); }
    private UdpBroadcaster createUdpBroadcaster() {
        var config = currentConfig;
        boolean legacy = config.ServerVersion.contains("1.8.") || config.ServerVersion.contains("1.7.");
        return new UdpBroadcaster("224.0.2.60", 4445, config.LocalPort, config.ForwardAddress, config.NickName, legacy);
    }
    private void restartUdpBroadcaster() {
        if (udpBroadcaster != null) udpBroadcaster.close();
        udpBroadcaster = createUdpBroadcaster();
        udpBroadcaster.startBroadcastingAsync();
    }
    public static void ensureLoaded() {
        if (Interceptor.class.getName().isEmpty()) throw new IllegalStateException("Should never call EnsureLoaded()");
    }
}
