package codexus.cipher.connection.protocols;

import codexus.cipher.connection.NetEaseConnection;
import codexus.csharpsup.DotNetEncoding;
import codexus.development.sdk.manager.IUserManager;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class AuthLibProtocol implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(AuthLibProtocol.class.getName());
    private final InetAddress address;
    private final int port;
    private final String modList, version, accessToken;
    private final Set<Socket> active = ConcurrentHashMap.newKeySet();
    private volatile boolean disposed;
    private ServerSocket listener;
    private CompletableFuture<Void> acceptLoop;
    public AuthLibProtocol(InetAddress address, int port, String modList, String version, String accessToken) {
        this.address = address; this.port = port; this.modList = modList; this.version = version; this.accessToken = accessToken;
    }
    public void start() {
        if (disposed) throw new IllegalStateException("AuthLibProtocol has been disposed");
        try { listener = new ServerSocket(); listener.bind(new InetSocketAddress(address, port)); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        acceptLoop = new CompletableFuture<>();
        Thread.ofVirtual().name("codexus-authlib-listener").start(() -> {
            try {
                while (!disposed) {
                    Socket client = listener.accept(); active.add(client);
                    Thread.ofVirtual().name("codexus-authlib-client").start(() -> handleClient(client));
                }
            } catch (IOException e) {
                if (!disposed) LOG.log(System.Logger.Level.WARNING, "Accept loop error: " + e.getMessage());
            } finally { acceptLoop.complete(null); }
        });
    }
    public void stop() { close(); }
    @Override public void close() {
        if (disposed) return;
        disposed = true;
        try { if (listener != null) listener.close(); }
        catch (IOException e) { LOG.log(System.Logger.Level.ERROR, "Authentication failed. " + e.getMessage()); }
        for (Socket client : active) {
            try { client.close(); } catch (IOException e) { LOG.log(System.Logger.Level.DEBUG, "Closing auth client", e); }
        }
        if (acceptLoop != null) {
            try { acceptLoop.get(5, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            catch (ExecutionException | TimeoutException e) { LOG.log(System.Logger.Level.ERROR, "Authentication failed. " + e.getMessage()); }
        }
    }
    private static byte[] readPayload(InputStream stream) throws IOException {
        byte[] length = new byte[4]; readExact(stream, length);
        int count = ByteBuffer.wrap(length).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte[] result = new byte[count]; readExact(stream, result); return result;
    }
    private static void readExact(InputStream stream, byte[] bytes) throws IOException {
        int total = 0;
        while (total < bytes.length) {
            int count = stream.read(bytes, total, bytes.length - total);
            if (count <= 0) throw new EOFException();
            total += count;
        }
    }
    private void handleClient(Socket client) {
        try (client) {
            InputStream stream = client.getInputStream();
            int[] responseCode = {1};
            try {
                String gameId = DotNetEncoding.utf8(readPayload(stream));
                String userId = DotNetEncoding.utf8(readPayload(stream));
                String certificate = new String(readPayload(stream), StandardCharsets.UTF_16LE);
                IUserManager manager = IUserManager.Current.Instance;
                var user = manager == null ? null : manager.getAvailableUser(userId);
                if (user == null) throw new IllegalStateException("User not found");
                if (!certificate.isEmpty()) NetEaseConnection.createAuthenticatorAsync(certificate, gameId, version,
                        modList, accessToken, Integer.parseInt(userId.strip()), user.AccessToken, () -> responseCode[0] = 0).join();
            } catch (Exception e) { LOG.log(System.Logger.Level.WARNING, "Client handling error: " + e.getMessage()); }
            try {
                if (!disposed) client.getOutputStream().write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(responseCode[0]).array());
            } catch (IOException e) { LOG.log(System.Logger.Level.WARNING, "Response writing error: " + e.getMessage()); }
        } catch (IOException e) { LOG.log(System.Logger.Level.WARNING, "Client handling error: " + e.getMessage()); }
        finally { active.remove(client); }
    }
}
