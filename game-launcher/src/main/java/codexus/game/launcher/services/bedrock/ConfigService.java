package codexus.game.launcher.services.bedrock;

import codexus.csharpsup.DotNetJson;
import codexus.game.launcher.utils.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ConfigService {
    private ConfigService() { }
    private static Map<String, Object> object(Object... fields) {
        var value = new LinkedHashMap<String, Object>(); for (int i = 0; i < fields.length; i += 2) value.put((String) fields[i], fields[i + 1]); return value;
    }
    public static void generateLaunchConfig(String skinPath, String roleName, String entityId, int port) { generateLaunchConfig(skinPath, roleName, entityId, port, null, null); }
    public static void generateLaunchConfig(String skinPath, String roleName, String entityId, int port, String outputDir, List<String> itemIds) {
        try {
            byte[] skin = Files.readAllBytes(Path.of(skinPath));
            String hash = codexus.cipher.extensions.MPayExtensions.encodeMd5(skin);
            var config = object("room_info", object("ip", "127.0.0.1", "port", Integer.toUnsignedLong(port), "room_name", "Codexus Server", "item_ids", itemIds == null ? List.of() : itemIds),
                    "player_info", object("user_id", 1, "user_name", roleName, "urs", "Codexus Server"),
                    "skin_info", object("skin", skinPath.replace("\\\\", "\\"), "hash", hash, "slim", false, "skin_iid", "100"),
                    "misc", object("multiplayer_game_type", 100, "auth_server_url", ""));
            Path directory = Path.of(outputDir == null ? PathUtil.CppGamePath : outputDir); Files.createDirectories(directory);
            Files.writeString(directory.resolve("mc.cfg"), DotNetJson.serialize(config));
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
