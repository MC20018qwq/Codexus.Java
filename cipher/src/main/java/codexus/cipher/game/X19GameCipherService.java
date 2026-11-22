package codexus.cipher.game;

import codexus.cipher.protocol.WPFLauncher;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.nio.file.Path;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/** Local X19 join protocol, recovered from Codexus_Server. No HTTP computation service is used. */
public final class X19GameCipherService {

    private static final String DEFAULT_CRC_SALT = "2AFA3B6116260DE4300577E013EAC757";
    private static final String PRC_CHECK = "[]";
    private static final byte[] MC_VERSION_SALT = {
            0x01, 0x00, 0x04, (byte) 0x80, (byte) 0xD2, 0x3E, (byte) 0xF7, 0x11, 0x01
    };
    private static final byte[] TCP_SALT = {
            0x2F, (byte) 0x84, (byte) 0xAE, (byte) 0xA3,
            (byte) 0x99, 0x21, 0x29, 0x26,
            (byte) 0xDA, (byte) 0xBF, (byte) 0x95, (byte) 0xA3,
            (byte) 0xAB, (byte) 0xAF, 0x37, (byte) 0xE0
    };
    private static final byte[] TOKEN_KEY = {
            (byte) 0xAC, 0x24, (byte) 0x9C, 0x69,
            (byte) 0xC7, 0x2C, (byte) 0xB3, (byte) 0xB4,
            0x4E, (byte) 0xC0, (byte) 0xCC, 0x6C,
            0x54, 0x3A, (byte) 0x81, (byte) 0x95
    };
    private static final byte[] SKIP32_KEY = "SaintSteve".getBytes(StandardCharsets.US_ASCII);

    private static final PublicKey AUTH_SERVER_PUBLIC_KEY = loadPublicKey(
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4HJFrYdVTeoSvH6qsnJElfXuf7FnxxFQdz3gRCs66LDrZfaoGoWt2e/aGIOv8uGHliBWnZP42Ike9Qf5aiYVtQ4mlj2bXZjifHG35LlS1Bq6yCA6k1WevWcrGWOuLzny3jo8Wbdi0lIFMTT2hN98sF2k4YcvyE9zhqxfRNFGVI5kLyxm9CeTKAXGBU5mw3yQWJ8cPRR4866jpGGOhBWlJdilWt2NES9bid8SbhTT55wqumnVO5J5/DaMyTgKIQngH7NyZQljAhdK5I23dzpGop322n2eQ+mTNLuquwU453o1cbyQobgC6vh5/F1QT2INBR2qYCnRzzJ6hrhE5kIMZwIDAQAB");
    private static final PrivateKey CLIENT_SIGNING_PRIVATE_KEY = loadPrivateKey(
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDobJGIddxcs08xzTIVFanc4/84J1DbxiW7wLokIrap3txzyyMXj+AcDa8jLopLJkMg9rZLzL50Dwp+hmTTgiIcjkM1DVREsRBltzqjVNyiYPL2VGHLn+/eEivjhLNWUMcWrlAoJ5JJBi22oGPLlKDVJpg33JPI5nPZpwufdB0ecn2V0CAeeGyswQyAaqIjoiYOoP3HipjEYGQHp1RsADf4ozGRgv+2HGiSOyhlv00ixnF0nRUTzVh18ka0N3LdLMpAN/YAPkO8tmoWp0asyU3X+nyZFd29povvrRy4rL1lYmo8jdpfpSL+Yk+9+RybjBAhXRx6uDoBxaa2kwE4fyrrAgMBAAECggEAWT42szruHfoLkofDjyz+R/6TZLBT788pdeoOjwl1McyMwTlihA2Oc7cdZFjeaPSMGgAhBwHarx2HXgWkeUIibuyBCcHQdX+3WBb+wPA4t3CaWdMUqecDZzV6/KVbZu0lRKQxyvlGxhtFOjZjmyu6hZ2IHQrpA97Y5N2rLNKcy69W+QYJZappmBfbVgWM0NRmgmpg6siQ0Cm6Ryil3SBAPBVv+EUPiD9jdXbtVq7VwN4YmwUGScp2Fib1oUnqEAja1hfihVnRFQ246nKXhIc/YVrNmwBrxAVwaaPFRka6XjKkSF0WVbpqQLXhbY1fS8FoXGpVhiF6o4rTQJbpQxhQgQKBgQD7XysfPNt9G63gTrkZvjvEk5LKsRG42MAYEkuzxEal9E/AQv0jJrk0f1WO09hCcYQeaOQXhM9mezNQv5jXEnmepXqM3NTw1Di5yh3uvjSXQjdUt+7haNw+QjggBqZxyQZjtadYairSzfmWe7OwJIkCmdgaJKxm4qMExk9kUgApGwKBgQDstBdJHU/KEBqVpsIlu185vFFuaAvxiHjXHqGwytJMQ/5aVqaphIiQCAxaEogPSzPSm28UHVQiZeFO059EKOpSJscW4pV95Dr5BAbHuYecacqnZKbQqb69//Cfpne9tGYXlmP6QnYfPoc4wYfTfPyU2x3KtDhVxtEDutpSNU1ycQKBgBabWn92s66uvJZ9vfvotetZ8ku0XQmoxK3lh1Vlg40NSdbar3Vn2CQ2h3VO7BYdq2oouMq8sQJgdh7+/DnreXChJUJh4ey+yVM8MDD2fjhURjGiUSOIkLYwsmd+8Z0uHRr+jUxQUAWhbJ7yBRkEUCYhu+OuBKtEGrElPKKjFUydAoGAQLj9pQBe0OGWY1U1wRt67k6P9aB9o42tfSTjEXRkDHaLFiibab7TmI6a0gY/Le9iPDREKzvZxY4WDXfQFNMbP1tbFObf+Yxuk6iGMhaI/jvvLdZXxrajcVCKex0JoNWzFMAKlmOV6PUwBFTmzu1eI1XGz6Z3wPycKmjtSY1JoAECgYBBOfaUDMaG1xLzv+q1jPPs2U4lXPK2BXFE5RaliUGC+LIQREXPishII2LYFW3gtXj5QWfIGq6x0d6ca6Bja2vYRDDe5tlT/2VbZahiHpb2PYL/2WgeoHl7sT9Bb/nsKyo85Sv+doop6huy4+aeTiQHgrGR9JYMVBSIx6P8Tt5phA==");

    private final String launcherVersion;
    private final String crcSalt;
    private final java.time.Clock clock;
    private final PublicKey authenticationPublicKey;
    private final GameArtifactResolver artifactResolver;
    private final boolean dynamicDownload;

    public X19GameCipherService() {
        this(WPFLauncher.getLatestVersionAsync().join());
    }

    public X19GameCipherService(String launcherVersion) {
        this(launcherVersion, DEFAULT_CRC_SALT, java.time.Clock.systemUTC());
    }

    public X19GameCipherService(String launcherVersion, String crcSalt, java.time.Clock clock) {
        this(launcherVersion, crcSalt, clock, AUTH_SERVER_PUBLIC_KEY);
    }

    public X19GameCipherService(Path minecraftDirectory) {
        this(WPFLauncher.getLatestVersionAsync().join(), DEFAULT_CRC_SALT,
                java.time.Clock.systemUTC(), AUTH_SERVER_PUBLIC_KEY, new GameArtifactResolver(minecraftDirectory));
    }

    public X19GameCipherService(String launcherVersion, String crcSalt, java.time.Clock clock, Path minecraftDirectory) {
        this(launcherVersion, crcSalt, clock, AUTH_SERVER_PUBLIC_KEY, new GameArtifactResolver(minecraftDirectory));
    }

    X19GameCipherService(String launcherVersion, String crcSalt, java.time.Clock clock, Path minecraftDirectory, boolean dynamicDownload) {
        this(launcherVersion, crcSalt, clock, AUTH_SERVER_PUBLIC_KEY, new GameArtifactResolver(minecraftDirectory), dynamicDownload);
    }

    X19GameCipherService(String launcherVersion, String crcSalt, java.time.Clock clock, PublicKey authenticationPublicKey) {
        this(launcherVersion, crcSalt, clock, authenticationPublicKey, new GameArtifactResolver(GameArtifactResolver.defaultMinecraftDirectory()));
    }

    private X19GameCipherService(String launcherVersion, String crcSalt, java.time.Clock clock, PublicKey authenticationPublicKey, GameArtifactResolver artifactResolver) {
        this(launcherVersion, crcSalt, clock, authenticationPublicKey, artifactResolver, GameAuthenticationMd5.DYNAMIC_DOWNLOAD);
    }

    private X19GameCipherService(String launcherVersion, String crcSalt, java.time.Clock clock, PublicKey authenticationPublicKey, GameArtifactResolver artifactResolver, boolean dynamicDownload) {
        this.dynamicDownload = dynamicDownload;
        this.artifactResolver = java.util.Objects.requireNonNull(artifactResolver, "artifactResolver");
        this.authenticationPublicKey = java.util.Objects.requireNonNull(authenticationPublicKey, "authenticationPublicKey");
        this.launcherVersion = requireText(launcherVersion, "X19 launcher version");
        this.crcSalt = requireText(crcSalt, "X19 CRC salt");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    public String authenticationBody(
            String serverId,
            long gameId,
            String gameVersion,
            String modInfo,
            String channel,
            int userId,
            String handshakeKey) {
        requireNonNegative(gameId, "gameId");
        requireNonNegative(userId, "userId");
        byte[] loginSeed = decodeBase64(handshakeKey, "handshakeKey");
        if (loginSeed.length != 16) {
            throw badRequest("handshakeKey must decode to 16 bytes");
        }

        String checkedVersion = requireText(gameVersion, "gameVersion");
        var md5 = GameAuthenticationMd5.resolve(checkedVersion, artifactResolver, dynamicDownload);

        String checkedModInfo = requireText(modInfo, "modInfo");
        String checkedServerId = requireText(serverId, "serverId");
        String checkedChannel = requireText(channel, "channel");
        int timestamp = Math.toIntExact(Instant.now(clock).getEpochSecond());
        byte[] hash = sha256(concat(
                (launcherVersion + checkedVersion + timestamp + crcSalt + checkedModInfo
                        + md5.bootstrapMd5() + md5.datFileMd5() + PRC_CHECK).getBytes(StandardCharsets.UTF_8),
                littleEndianInt(userId),
                loginSeed));

        LegacyBuffer body = new LegacyBuffer();
        body.writeLong(gameId);
        body.writeByteString(checkedServerId);
        body.writeByteString(launcherVersion);
        body.writeByteString(checkedVersion);
        body.writeInt(timestamp);
        body.write(hash);
        body.writeShortString(checkedModInfo, true);
        body.writeShortString(PRC_CHECK, true);
        body.writeShort(0, true);
        body.writeByteString(checkedChannel);
        return Base64.getEncoder().encodeToString(body.toByteArray());
    }

    public String authenticationHandshake(
            int userId,
            String userToken,
            String base64Context,
            String channel,
            String gameVersion) {
        requireNonNegative(userId, "userId");
        byte[] context = decodeBase64(base64Context, "base64Context");
        if (context.length != 272) {
            throw badRequest("base64Context must decode to 272 bytes");
        }
        byte[] token = asciiToken(userToken);
        String checkedChannel = requireText(channel, "channel");
        String checkedVersion = requireText(gameVersion, "gameVersion");

        byte[] loginSeed = Arrays.copyOfRange(context, 0, 16);
        byte[] signContent = Arrays.copyOfRange(context, 16, context.length);
        int authId = Skip32.encrypt(userId, SKIP32_KEY);
        byte[] encryptedSeed = aesEcb(loginSeed, xor(token, TOKEN_KEY));

        byte[] sign = sha256(concat(
                littleEndianInt(authId),
                encryptedSeed,
                launcherVersion.getBytes(StandardCharsets.UTF_8),
                checkedChannel.getBytes(StandardCharsets.UTF_8),
                crcSalt.getBytes(StandardCharsets.UTF_8),
                checkedVersion.getBytes(StandardCharsets.UTF_8),
                MC_VERSION_SALT));

        byte[] signedContext = rsa(Cipher.DECRYPT_MODE, authenticationPublicKey, signContent);
        if (signedContext.length < 51) {
            throw badRequest("Invalid X19 authentication context");
        }
        byte[] clientKey = Arrays.copyOfRange(signedContext, 0, 19);
        byte[] expectedChecksum = sha256(loginSeed);
        byte[] checksum = Arrays.copyOfRange(signedContext, 19, 51);
        if (!MessageDigest.isEqual(checksum, expectedChecksum)) {
            throw badRequest("X19 authentication context checksum failed");
        }

        byte[] signData = rsa(Cipher.ENCRYPT_MODE, CLIENT_SIGNING_PRIVATE_KEY, concat(clientKey, sign));
        LegacyBuffer payload = new LegacyBuffer();
        payload.writeInt(authId);
        payload.write(encryptedSeed);
        payload.writeShortString(launcherVersion, false);
        payload.writeByteString(checkedChannel);
        payload.write(TCP_SALT);
        payload.writeShortBytes(signData, true);
        payload.writeByteString(checkedVersion);
        payload.write(MC_VERSION_SALT);

        LegacyBuffer message = new LegacyBuffer();
        message.writeShort(payload.size(), true);
        message.write(payload.toByteArray());
        return Base64.getEncoder().encodeToString(message.toByteArray());
    }

    private static byte[] asciiToken(String userToken) {
        String value = requireText(userToken, "userToken");
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length != 16 || !new String(bytes, StandardCharsets.US_ASCII).equals(value)) {
            throw badRequest("userToken must contain exactly 16 ASCII bytes");
        }
        return bytes;
    }

    private static byte[] aesEcb(byte[] value, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build X19 AES payload", exception);
        }
    }

    private static byte[] rsa(int mode, java.security.Key key, byte[] value) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(mode, key);
            return cipher.doFinal(value);
        } catch (Exception exception) {
            if (mode == Cipher.DECRYPT_MODE) {
                throw badRequest("Invalid X19 authentication context");
            }
            throw new IllegalStateException("Unable to build X19 RSA payload", exception);
        }
    }

    private static PublicKey loadPublicKey(String base64) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static PrivateKey loadPrivateKey(String base64) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static byte[] decodeBase64(String value, String field) {
        try {
            return Base64.getDecoder().decode(requireText(value, field));
        } catch (IllegalArgumentException exception) {
            throw badRequest(field + " must be valid Base64");
        }
    }

    private static byte[] concat(byte[]... values) {
        int length = Arrays.stream(values).mapToInt(value -> value.length).sum();
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] value : values) {
            System.arraycopy(value, 0, result, offset, value.length);
            offset += value.length;
        }
        return result;
    }

    private static byte[] xor(byte[] value, byte[] key) {
        byte[] result = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = (byte) (value[i] ^ key[i]);
        }
        return result;
    }

    private static byte[] littleEndianInt(int value) {
        return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw badRequest(field + " is required");
        }
        return value;
    }

    private static void requireNonNegative(long value, String field) {
        if (value < 0) {
            throw badRequest(field + " must not be negative");
        }
    }

    private static IllegalArgumentException badRequest(String message) {
        return new IllegalArgumentException(message);
    }

    private static final class LegacyBuffer {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        int size() {
            return output.size();
        }

        void write(byte[] value) {
            output.writeBytes(value);
        }

        void writeInt(int value) {
            write(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
        }

        void writeLong(long value) {
            write(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array());
        }

        void writeShort(int value, boolean littleEndian) {
            if (value < 0 || value > 0xFFFF) {
                throw badRequest("X19 payload field exceeds the UInt16 limit");
            }
            ByteOrder order = littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            write(ByteBuffer.allocate(Short.BYTES).order(order).putShort((short) value).array());
        }

        void writeByteString(String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 0xFF) {
                throw badRequest("X19 payload field exceeds the byte-length limit");
            }
            output.write(bytes.length);
            write(bytes);
        }

        void writeShortString(String value, boolean littleEndian) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writeShort(bytes.length, littleEndian);
            write(bytes);
        }

        void writeShortBytes(byte[] value, boolean littleEndian) {
            writeShort(value.length, littleEndian);
            write(value);
        }

        byte[] toByteArray() {
            return output.toByteArray();
        }
    }

    private static final class Skip32 {
        private static final int[] F_TABLE = {
                0xa3, 0xd7, 0x09, 0x83, 0xf8, 0x48, 0xf6, 0xf4, 0xb3, 0x21, 0x15, 0x78, 0x99, 0xb1,
                0xaf, 0xf9, 0xe7, 0x2d, 0x4d, 0x8a, 0xce, 0x4c, 0xca, 0x2e, 0x52, 0x95, 0xd9, 0x1e,
                0x4e, 0x38, 0x44, 0x28, 0x0a, 0xdf, 0x02, 0xa0, 0x17, 0xf1, 0x60, 0x68, 0x12, 0xb7,
                0x7a, 0xc3, 0xe9, 0xfa, 0x3d, 0x53, 0x96, 0x84, 0x6b, 0xba, 0xf2, 0x63, 0x9a, 0x19,
                0x7c, 0xae, 0xe5, 0xf5, 0xf7, 0x16, 0x6a, 0xa2, 0x39, 0xb6, 0x7b, 0x0f, 0xc1, 0x93,
                0x81, 0x1b, 0xee, 0xb4, 0x1a, 0xea, 0xd0, 0x91, 0x2f, 0xb8, 0x55, 0xb9, 0xda, 0x85,
                0x3f, 0x41, 0xbf, 0xe0, 0x5a, 0x58, 0x80, 0x5f, 0x66, 0x0b, 0xd8, 0x90, 0x35, 0xd5,
                0xc0, 0xa7, 0x33, 0x06, 0x65, 0x69, 0x45, 0x00, 0x94, 0x56, 0x6d, 0x98, 0x9b, 0x76,
                0x97, 0xfc, 0xb2, 0xc2, 0xb0, 0xfe, 0xdb, 0x20, 0xe1, 0xeb, 0xd6, 0xe4, 0xdd, 0x47,
                0x4a, 0x1d, 0x42, 0xed, 0x9e, 0x6e, 0x49, 0x3c, 0xcd, 0x43, 0x27, 0xd2, 0x07, 0xd4,
                0xde, 0xc7, 0x67, 0x18, 0x89, 0xcb, 0x30, 0x1f, 0x8d, 0xc6, 0x8f, 0xaa, 0xc8, 0x74,
                0xdc, 0xc9, 0x5d, 0x5c, 0x31, 0xa4, 0x70, 0x88, 0x61, 0x2c, 0x9f, 0x0d, 0x2b, 0x87,
                0x50, 0x82, 0x54, 0x64, 0x26, 0x7d, 0x03, 0x40, 0x34, 0x4b, 0x1c, 0x73, 0xd1, 0xc4,
                0xfd, 0x3b, 0xcc, 0xfb, 0x7f, 0xab, 0xe6, 0x3e, 0x5b, 0xa5, 0xad, 0x04, 0x23, 0x9c,
                0x14, 0x51, 0x22, 0xf0, 0x29, 0x79, 0x71, 0x7e, 0xff, 0x8c, 0x0e, 0xe2, 0x0c, 0xef,
                0xbc, 0x72, 0x75, 0x6f, 0x37, 0xa1, 0xec, 0xd3, 0x8e, 0x62, 0x8b, 0x86, 0x10, 0xe8,
                0x08, 0x77, 0x11, 0xbe, 0x92, 0x4f, 0x24, 0xc5, 0x32, 0x36, 0x9d, 0xcf, 0xf3, 0xa6,
                0xbb, 0xac, 0x5e, 0x6c, 0xa9, 0x13, 0x57, 0x25, 0xb5, 0xe3, 0xbd, 0xa8, 0x3a, 0x01,
                0x05, 0x59, 0x2a, 0x46
        };

        static int encrypt(int value, byte[] key) {
            int[] buffer = {
                    (value >>> 24) & 0xff,
                    (value >>> 16) & 0xff,
                    (value >>> 8) & 0xff,
                    value & 0xff
            };
            int left = (buffer[0] << 8) + buffer[1];
            int right = (buffer[2] << 8) + buffer[3];
            int round = 0;
            for (int i = 0; i < 12; i++) {
                right ^= g(key, round, left) ^ round;
                round++;
                left ^= g(key, round, right) ^ round;
                round++;
            }
            return ((right >>> 8) << 24)
                    | ((right & 0xff) << 16)
                    | ((left >>> 8) << 8)
                    | (left & 0xff);
        }

        private static int g(byte[] key, int round, int word) {
            int g1 = word >>> 8;
            int g2 = word & 0xff;
            int g3 = F_TABLE[g2 ^ (key[(4 * round) % 10] & 0xff)] ^ g1;
            int g4 = F_TABLE[g3 ^ (key[(4 * round + 1) % 10] & 0xff)] ^ g2;
            int g5 = F_TABLE[g4 ^ (key[(4 * round + 2) % 10] & 0xff)] ^ g3;
            int g6 = F_TABLE[g5 ^ (key[(4 * round + 3) % 10] & 0xff)] ^ g4;
            return (g5 << 8) + g6;
        }
    }
}
