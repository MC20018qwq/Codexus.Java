package codexus.heypixel.plugin.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public final class HeypixelKeyDerivationService {

    static final Version V1 = new Version("4152", List.of(
            new Rule(1, 0, 2), new Rule(2, 2, 1), new Rule(4, 4, 3)));
    static final Version V2 = new Version("cf0d", List.of(
            new Rule(1, 1, 1), new Rule(2, 1, 2), new Rule(4, 2, 3)));
    static final Version V3 = new Version("e398", List.of(
            new Rule(1, 1, 3), new Rule(2, 1, 2), new Rule(4, 2, 1)));
    static final Version V4 = new Version("ab76", List.of(
            new Rule(1, 1, 2), new Rule(2, 1, 2), new Rule(4, 2, 2)));

    private static final List<Version> VERSIONS = List.of(V1, V2, V3, V4);

    public UUID derive(UUID profile, long userId) {
        Version version = VERSIONS.get(new Random().nextInt(VERSIONS.size()));
        char nibble = version.nibbles().charAt(new Random().nextInt(version.nibbles().length()));
        return derive(profile, userId, UUID.randomUUID(), version, nibble);
    }

    static UUID derive(UUID profile, long userId, UUID template, Version version, char nibble) {
        if (version.nibbles().indexOf(nibble) < 0) {
            throw new IllegalArgumentException("Nibble does not belong to the selected version");
        }

        String[] parts = template.toString().split("-");
        StringBuilder first = new StringBuilder(parts[0]);
        first.setCharAt(2, nibble);
        parts[0] = first.toString();

        String digest = digest(profile, normalizeUserId(userId), nibble);
        for (int index = 0; index < version.rules().size(); index++) {
            Rule rule = version.rules().get(index);
            int digestStart = index * rule.length();
            String fragment = digest.substring(digestStart, digestStart + rule.length());
            StringBuilder segment = new StringBuilder(parts[rule.segment()]);
            segment.replace(rule.start(), rule.start() + rule.length(), fragment);
            parts[rule.segment()] = segment.toString();
        }

        return UUID.fromString(String.join("-", parts));
    }

    static long normalizeUserId(long userId) {
        return (userId & 0x80000000L) == 0L ? userId | 0x80000000L : userId;
    }

    private static String digest(UUID profile, long normalizedUserId, char nibble) {
        String input = profile + ":" + normalizedUserId + ":" + nibble;
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(bytes, 0, 8);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    record Version(String nibbles, List<Rule> rules) {
    }

    private record Rule(int segment, int start, int length) {
    }
}

