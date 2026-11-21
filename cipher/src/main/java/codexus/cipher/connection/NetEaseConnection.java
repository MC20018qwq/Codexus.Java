package codexus.cipher.connection;

import codexus.cipher.connection.chacha.ChaChaOfSalsa;
import codexus.cipher.entities.connection.*;
import codexus.cipher.extensions.*;
import codexus.cipher.utils.UrlsUtil;
import codexus.cipher.utils.http.HttpWrapper;
import codexus.csharpsup.*;
import codexus.cipher.game.LocalGameCipherProvider;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public final class NetEaseConnection {
    private static final byte[] TOKEN_KEY = HexFormat.of().parseHex("ac249c69c72cb3b44ec0cc6c543a8195");
    private static final byte[] NONCE = DotNetEncoding.ascii("163 NetEase\n");
    private NetEaseConnection() { }
    @FunctionalInterface public interface BuildEstablishing {
        byte[] build(String nexusToken, String gameVersion, int userId, String userToken, byte[] context, String channel);
    }
    @FunctionalInterface public interface BuildJoinServerMessage {
        byte[] build(String nexusToken, ChaChaOfSalsa cipher, String serverId, long gameId, String gameVersion,
                     String modInfo, String channel, int userId, byte[] handshakeKey);
    }
    public static CompletableFuture<YggdrasilServer> randomAuthServerAsync() {
        var http = new HttpWrapper();
        return http.getAsync(UrlsUtil.AuthServerUrl).thenApply(response -> {
            HttpWrapper.ensureSuccess(response);
            var servers = DotNetJson.deserialize(HttpWrapper.readAsString(response), YggdrasilServer[].class);
            if (servers == null || servers.length == 0) throw new IllegalStateException("No authentication servers found.");
            var server = servers[ThreadLocalRandom.current().nextInt(servers.length)];
            if (server.Ip == null || server.Ip.isBlank() || server.Port <= 0 || server.Port > 65535)
                throw new IllegalArgumentException("The authentication server list contains an invalid endpoint.");
            return server;
        }).whenComplete((value, error) -> http.close());
    }
    public static CompletableFuture<Void> createAuthenticatorAsync(String serverId, String gameId, String gameVersion,
            String modInfo, String nexusToken, int userId, String userToken, Runnable handleSuccess) {
        return createAuthenticatorAsync(serverId, gameId, gameVersion, modInfo, nexusToken, userId, userToken, handleSuccess, null, null);
    }
    public static CompletableFuture<Void> createAuthenticatorAsync(String serverId, String gameId, String gameVersion,
            String modInfo, String nexusToken, int userId, String userToken, Runnable handleSuccess,
            BuildEstablishing establishing, BuildJoinServerMessage joinServer) {
        return randomAuthServerAsync().thenCompose(server -> createAuthenticatorAsync(serverId, gameId, gameVersion,
                modInfo, nexusToken, userId, userToken, server.Ip, server.Port, handleSuccess, establishing, joinServer));
    }
    public static CompletableFuture<Void> createAuthenticatorAsync(String serverId, String gameId, String gameVersion,
            String modInfo, String nexusToken, int userId, String userToken, String authAddress, int authPort,
            Runnable handleSuccess, BuildEstablishing establishing, BuildJoinServerMessage joinServer) {
        Objects.requireNonNull(handleSuccess, "handleSuccess");
        CompletableFuture<Void> result = new CompletableFuture<>();
        Socket client = new Socket();
        result.whenComplete((value, error) -> {
            if (result.isCancelled()) try { client.close(); } catch (IOException ignored) { }
        });
        Thread.ofVirtual().name("codexus-authentication").start(() -> {
            try (client; var localCipher = new LocalGameCipherProvider()) {
                client.connect(new InetSocketAddress(InetAddress.ofLiteral(authAddress), authPort), 10000);
                client.setSoTimeout(30000);
                InputStream stream = client.getInputStream();
                byte[] details = StreamExtensions.readSteamWithInt16(stream).readAllBytes();
                if (details.length != 272) throw new EOFException("Connection details do not contain the full key and certificate.");
                byte[] remoteKey = Arrays.copyOf(details, 16);
                byte[] establishingPayload = establishing == null
                        ? defaultBuildEstablishing(localCipher, gameVersion, userId, userToken, details, "netease")
                        : establishing.build(nexusToken, gameVersion, userId, userToken, details, "netease");
                client.getOutputStream().write(establishingPayload);
                byte[] statusBytes = StreamExtensions.readSteamWithInt16(stream).readAllBytes();
                if (statusBytes.length == 0) throw new EOFException("Missing establishing status.");
                int status = statusBytes[0] & 255;
                if (status != 0) throw new IllegalStateException(String.format("Establishing error: %02X", status));
                byte[] tokenKey = ByteArrayExtensions.xor(DotNetEncoding.ascii(userToken), TOKEN_KEY);
                var encrypt = new ChaChaOfSalsa(ByteArrayExtensions.combineWith(tokenKey, remoteKey), NONCE, true);
                var decrypt = new ChaChaOfSalsa(ByteArrayExtensions.combineWith(remoteKey, tokenKey), NONCE, false);
                byte[] message = joinServer == null
                        ? defaultBuildJoinServerMessage(localCipher, encrypt, serverId, Long.parseLong(gameId.strip()), gameVersion, modInfo, "netease", userId, remoteKey)
                        : joinServer.build(nexusToken, encrypt, serverId, Long.parseLong(gameId.strip()), gameVersion, modInfo, "netease", userId, remoteKey);
                client.getOutputStream().write(message);
                byte[] response = StreamExtensions.readSteamWithInt16(stream).readAllBytes();
                var unpacked = ChaChaExtensions.unpackMessage(decrypt, response);
                if (unpacked.data().length == 0) throw new EOFException("Missing authentication result.");
                if (unpacked.type() != 9 || unpacked.data()[0] != 0)
                    throw new IllegalStateException("Authentication of message failed: " + (unpacked.data()[0] & 255));
                handleSuccess.run();
                result.complete(null);
            } catch (Exception error) {
                Throwable cause = error;
                while (cause instanceof CompletionException && cause.getCause() != null) cause = cause.getCause();
                result.completeExceptionally(new IllegalStateException("Failed to create connection: " + cause.getMessage(), cause));
            }
        });
        return result;
    }
    private static byte[] defaultBuildEstablishing(LocalGameCipherProvider localCipher, String gameVersion, int userId,
                                                   String userToken, byte[] context, String channel) {
        return Base64.getDecoder().decode(localCipher.authenticationHandshake(
                userId, userToken, Base64.getEncoder().encodeToString(context), channel, gameVersion));
    }
    private static byte[] defaultBuildJoinServerMessage(LocalGameCipherProvider localCipher, ChaChaOfSalsa cipher, String serverId,
            long gameId, String gameVersion, String modInfo, String channel, int userId, byte[] handshakeKey) {
        String body = localCipher.authenticationBody(serverId, gameId, gameVersion,
                modInfo, channel, userId, Base64.getEncoder().encodeToString(handshakeKey));
        return ChaChaExtensions.packMessage(cipher, (byte) 9, Base64.getDecoder().decode(body));
    }
}
