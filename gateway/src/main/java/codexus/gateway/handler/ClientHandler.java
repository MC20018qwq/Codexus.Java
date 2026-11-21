package codexus.gateway.handler;

import codexus.cipher.protocol.*;
import codexus.csharpsup.*;
import codexus.gateway.connection.*;
import codexus.gateway.entities.*;
import codexus.gateway.extensions.StringExtensions;
import codexus.gateway.manager.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class ClientHandler implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(ClientHandler.class.getName());
    private final ConcurrentMap<UUID, SecureConnection> connections = new ConcurrentHashMap<>();
    private final MessageManager messages;
    private final UserManager users;
    private final Supplier<WPFLauncher> launcherFactory;
    private WPFLauncher launcher;
    private Com4399 com4399;
    private Pc4399 pc4399;
    private boolean closed;
    private static final Object QUERY_LOCK = new Object();
    private static volatile boolean queryInitialized;
    private static void ensureQuery() {
        synchronized (QUERY_LOCK) {
            if (!queryInitialized) { InternalQuery.initialize(); queryInitialized = true; }
        }
    }
    private static WPFLauncher createLauncher() { ensureQuery(); return new WPFLauncher(); }
    public ClientHandler() { this(UserManager.Instance, ClientHandler::createLauncher, new MessageManager()); }
    public ClientHandler(UserManager users, Supplier<WPFLauncher> launcherFactory, MessageManager messages) {
        this.users = users; this.launcherFactory = launcherFactory; this.messages = messages;
    }
    public UserManager getUsers() { return users; }
    public synchronized WPFLauncher getX19() { checkOpen(); if (launcher == null) launcher = launcherFactory.get(); return launcher; }
    public synchronized Com4399 getCom4399() { checkOpen(); if (com4399 == null) { ensureQuery(); com4399 = new Com4399(); } return com4399; }
    public synchronized Pc4399 getPc4399() {
        checkOpen();
        if (pc4399 == null) {
            ensureQuery();
            var pc = new Pc4399();
            try { pc.MPay.configure("EEkEEXLymcNjM42yLY3Bn6AO15aGy4yq", "4399pc").join(); pc4399 = pc; }
            catch (RuntimeException error) { pc.close(); throw error; }
        }
        return pc4399;
    }
    private void checkOpen() { if (closed) throw new IllegalStateException("Gateway client handler closed"); }
    public synchronized SecureConnection onClientConnected(UUID id, Consumer<byte[]> sender, Consumer<String> closer) {
        checkOpen(); var connection = new SecureConnection(id, sender, closer); connections.put(id, connection); return connection;
    }
    public void onClientDisconnected(UUID id) { var connection = connections.remove(id); if (connection != null) connection.close(); }
    public void onMessageReceived(UUID id, byte[] bytes) {
        var connection = connections.get(id); if (connection == null || connection.IsHandshaking) return;
        try {
            Entity entity = DotNetJson.deserialize(DotNetEncoding.utf8(connection.decode(bytes)), Entity.class);
            if (entity == null || entity.Type == null || entity.Payload == null) return;
            if (!Objects.equals(StringExtensions.toSha256(entity.Payload), entity.Sign)) return;
            connection.CurrentIdentify.set(entity.Identify);
            var message = messages.buildMessage(connection.State, entity.Type); if (message == null) return;
            message.readFromMessage(this, entity.Payload); message.handleMessage(this, connection);
        } catch (Exception e) { LOG.log(System.Logger.Level.WARNING, "Unable to process Gateway message", e); error(connection, e); }
        finally { connection.CurrentIdentify.remove(); }
    }
    public void broadcastMessage(Entity entity) { for (var connection : connections.values()) connection.sendToClientAsync(entity); }
    public static void send(SecureConnection connection, String type, String payload) { connection.sendToClientAsync(new Entity(type, payload)); }
    public static void error(SecureConnection connection, Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) error = error.getCause();
        send(connection, "error_notification", error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
    }
    @Override public synchronized void close() {
        if (closed) return; closed = true;
        connections.keySet().forEach(this::onClientDisconnected);
        if (launcher != null) launcher.close(); if (com4399 != null) com4399.close(); if (pc4399 != null) pc4399.close();
    }
}
