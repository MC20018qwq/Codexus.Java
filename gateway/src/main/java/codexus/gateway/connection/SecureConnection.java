package codexus.gateway.connection;

import codexus.csharpsup.*;
import codexus.gateway.entities.Entity;
import codexus.gateway.extensions.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.function.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public final class SecureConnection implements AutoCloseable {
    public final UUID ClientId;
    public volatile ConnectionState State = ConnectionState.Establishing;
    public volatile boolean IsHandshaking, IsEncrypted;
    public final ThreadLocal<String> CurrentIdentify = new ThreadLocal<>();
    private byte[] sessionKey;
    private boolean closed;
    private KeyPair exchange;
    private final Consumer<byte[]> sender;
    private final Consumer<String> closer;
    public SecureConnection(UUID clientId, Consumer<byte[]> sender, Consumer<String> closer) {
        ClientId = Objects.requireNonNull(clientId); this.sender = Objects.requireNonNull(sender); this.closer = Objects.requireNonNull(closer);
        try {
            var generator = KeyPairGenerator.getInstance("EC"); generator.initialize(EcCurveExtensions.DefaultCurve);
            exchange = generator.generateKeyPair();
        } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }
    public synchronized void handleHandshaking(PublicKey clientKey) {
        if (closed || exchange == null || IsEncrypted) throw new IllegalStateException("Handshake already completed");
        IsHandshaking = true;
        try {
            var agreement = KeyAgreement.getInstance("ECDH"); agreement.init(exchange.getPrivate()); agreement.doPhase(clientKey, true);
            byte[] secret = agreement.generateSecret();
            try { sessionKey = deriveSessionKey(secret); } finally { Arrays.fill(secret, (byte) 0); }
            sendToClientAsync(new Entity("handshake", Base64.getEncoder().encodeToString(exchange.getPublic().getEncoded())));
            exchange = null; IsEncrypted = true; State = ConnectionState.Connected;
        } catch (GeneralSecurityException e) { throw new IllegalArgumentException("Handshake failed", e); }
        finally { IsHandshaking = false; }
    }
    public static byte[] deriveSessionKey(byte[] secret) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec("codexus.today.websocket.establishing".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] prk = mac.doFinal(secret);
            try {
                mac.init(new SecretKeySpec(prk, "HmacSHA256"));
                mac.update("codexus.today.aes.key".getBytes(StandardCharsets.UTF_8)); mac.update((byte) 1);
                return mac.doFinal();
            } finally { Arrays.fill(prk, (byte) 0); }
        } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }
    public synchronized byte[] decode(byte[] message) { if (closed) throw new IllegalStateException("Connection closed"); return IsEncrypted ? CipherExtensions.decrypt(message, sessionKey) : message; }
    public void sendToClientAsync(Entity entity) {
        if (entity == null) { sendToClientAsync("null"); return; }
        var reply = new Entity(); reply.Type = entity.Type; reply.Payload = entity.Payload;
        reply.prepareSignatureAndIdentify(CurrentIdentify.get());
        sendToClientAsync(DotNetJson.serialize(reply));
    }
    public void sendToClientAsync(String message) { sendToClientAsync(DotNetEncoding.utf8(message)); }
    public synchronized void sendToClientAsync(byte[] message) { if (closed) return; sender.accept(IsEncrypted ? CipherExtensions.encrypt(message, sessionKey) : message); }
    public void closeAsync(String reason) { closer.accept(reason); }
    @Override public synchronized void close() {
        if (closed) return; closed = true;
        exchange = null; if (sessionKey != null) Arrays.fill(sessionKey, (byte) 0); sessionKey = null;
        CurrentIdentify.remove();
    }
}
