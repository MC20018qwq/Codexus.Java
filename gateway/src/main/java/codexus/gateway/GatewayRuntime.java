package codexus.gateway;

import codexus.development.sdk.manager.*;
import codexus.game.launcher.utils.PathUtil;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.manager.*;
import codexus.gateway.utils.HostingUtil;
import codexus.gateway.websocket.WebSocketServer;
import codexus.interceptors.Interceptor;
import java.io.IOException;
import java.nio.file.*;

/** Owns all local listeners and the account/game resources used by the Gateway. */
public final class GatewayRuntime implements AutoCloseable {
    private final UserManager users;
    private final ClientHandler handler;
    private final WebSocketServer websocket;
    private final HostingUtil website;
    private boolean closed;
    public GatewayRuntime(UserManager users, ClientHandler handler, Path webRoot, Path skins, Path settings, int httpPort, int websocketPort, boolean listenAll) throws IOException {
        this.users = users; this.handler = handler;
        websocket = new WebSocketServer(websocketPort, "/gateway", handler);
        HostingUtil host = null;
        try {
            host = new HostingUtil(webRoot, skins, settings, httpPort);
            if (!websocket.startAsync(listenAll).join()) throw new IOException("Failed to start Gateway WebSocket server");
            website = host;
        } catch (Exception error) {
            websocket.close(); if (host != null) host.close(); handler.close();
            if (error instanceof IOException io) throw io;
            throw new IOException("Gateway startup failed", error);
        }
    }
    public static GatewayRuntime start(boolean listenAll) throws IOException {
        Files.createDirectories(Path.of(PathUtil.CustomModsPath)); Files.createDirectories(Path.of(PathUtil.SkinsPath));
        UserManager.Instance.readUsersFromDisk();
        Interceptor.ensureLoaded(); PacketManager.Instance.ensureRegistered();
        try {
        PluginManager.Instance.ensureUninstall(); PluginManager.Instance.loadPlugins("plugins");
        GameManager.Instance.registerActions();
        var handler = new ClientHandler();
        return new GatewayRuntime(UserManager.Instance, handler, Path.of(PathUtil.WebSitePath), Path.of(PathUtil.SkinsPath),
                Path.of(PathUtil.BasePath, "web_settings.json"), 3550, 19541, listenAll);
        } catch (IOException | RuntimeException | Error error) {
            PluginManager.Instance.close();
            UserManager.Instance.close();
            throw error;
        }
    }
    public int getWebSocketPort() { return websocket.getPort(); }
    public int getHttpPort() { return website.getPort(); }
    @Override public synchronized void close() {
        if (closed) return; closed = true;
        websocket.close(); website.close();
        try { GameManager.Instance.close(); } finally {
            try { PluginManager.Instance.close(); } finally { try { handler.close(); } finally { users.close(); } }
        }
    }
}
