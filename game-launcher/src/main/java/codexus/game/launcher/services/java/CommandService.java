package codexus.game.launcher.services.java;

import codexus.csharpsup.*;
import codexus.cipher.entities.wpflauncher.minecraft.EntityUserPropertiesEx;
import codexus.cipher.entities.wpflauncher.netgame.*;
import codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType;
import codexus.cipher.utils.RandomUtil;
import codexus.game.launcher.utils.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

public final class CommandService {
    private static final Set<EnumGameVersion> MODERN_ARGUMENTS = EnumSet.of(EnumGameVersion.V_1_13_2, EnumGameVersion.V_1_14_3,
            EnumGameVersion.V_1_15, EnumGameVersion.V_1_16, EnumGameVersion.V_1_18, EnumGameVersion.V_1_19_2,
            EnumGameVersion.V_1_20, EnumGameVersion.V_1_20_6, EnumGameVersion.V_1_21,
            EnumGameVersion.V_1_21_8, EnumGameVersion.V_1_21_10, EnumGameVersion.V_1_21_11);
    private final List<String> arguments = new ArrayList<>();
    private final List<String> jars = new ArrayList<>();
    private EnumGameVersion gameVersion;
    private String version, roleName, uuid, userId, protocolVersion, workPath;
    private int rpcPort;
    private int javaMajorVersion;
    private boolean isFilter;
    public boolean init(EnumGameVersion gameVersion, int maxMemory, String roleName, String serverIp, int serverPort,
                        String userId, String dToken, String gameId, String workPath, String uuid, int socketPort) {
        return init(gameVersion, maxMemory, roleName, serverIp, serverPort, userId, dToken, gameId, workPath, uuid, socketPort, "", true, 11413);
    }
    public boolean init(EnumGameVersion gameVersion, int maxMemory, String roleName, String serverIp, int serverPort,
                        String userId, String dToken, String gameId, String workPath, String uuid, int socketPort,
                        String protocolVersion, boolean isFilter, int rpcPort) {
        this.gameVersion = Objects.requireNonNull(gameVersion); this.version = GameVersionUtil.getGameVersionFromEnum(gameVersion);
        this.javaMajorVersion = JavaRuntimeUtil.baseline(gameVersion);
        this.roleName = roleName; this.uuid = uuid; this.userId = userId; this.protocolVersion = protocolVersion;
        this.workPath = workPath; this.rpcPort = rpcPort; this.isFilter = isFilter;
        Path config = Path.of(PathUtil.GameBasePath, ".minecraft", "versions", version, version + ".json");
        if (!Files.isRegularFile(config)) throw new IllegalStateException("Game version JSON not found, please go to Setting to fix the game file and try again.");
        try {
            var mapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
            JsonNode cfg = mapper.readTree(Files.readAllBytes(config));
            if (cfg == null || cfg.isNull()) throw new IllegalArgumentException("Game version JSON is empty.");
            buildJarLists(cfg); arguments.clear();
            arguments.add("-Djava.library.path=" + minecraft("versions", version, "natives"));
            arguments.add("-Druntime_path=" + minecraft("versions", version, "natives", "runtime"));
            arguments.addAll(List.of("-DlauncherControlPort=" + socketPort, "-DlauncherGameId=" + gameId, "-DuserId=" + userId, "-DToken=" + dToken, "-DServer=RELEASE"));
            if (MODERN_ARGUMENTS.contains(gameVersion)) buildModern(cfg, maxMemory);
            else buildLegacy(cfg, maxMemory);
            arguments.addAll(List.of("--userPropertiesEx", userPropertiesEx(), "--server", serverIp, "--port", Integer.toString(serverPort)));
            return true;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public void useJavaVersion(int major) {
        JavaRuntimeUtil.directoryName(major);
        if (major < JavaRuntimeUtil.baseline(gameVersion)) throw new IllegalArgumentException("Java version is below the game requirement");
        javaMajorVersion = major;
    }
    public String getJavaExecutable() { return JavaRuntimeUtil.executable(javaMajorVersion).toString(); }
    public List<String> getArguments() { return List.copyOf(arguments); }
    public Process startGame() {
        var command = new ArrayList<String>(); command.add(getJavaExecutable()); command.addAll(arguments);
        try { return new ProcessBuilder(command).directory(Path.of(workPath).toFile()).start(); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private static String minecraft(String... relative) { return Path.of(PathUtil.GameBasePath, ".minecraft").resolve(Path.of("", relative)).toString(); }
    private void buildJarLists(JsonNode cfg) {
        jars.clear();
        if (cfg.path("libraries").isArray()) for (var library : cfg.path("libraries")) {
            if (!library.has("name")) continue;
            String name = library.path("name").isNull() ? null : library.path("name").asText();
            String[] parts = name == null ? null : name.split(":", -1);
            if (parts != null && parts.length >= 3 && !parts[1].contains("platform"))
                jars.add(minecraft("libraries", parts[0].replace('.', File.separatorChar), parts[1], parts[2], parts[1] + "-" + parts[2] + ".jar"));
        }
        jars.add(minecraft("versions", version, version + ".jar"));
    }
    private void buildLegacy(JsonNode cfg, int memory) {
        arguments.addAll(List.of("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump", "-Xmx" + memory + "M",
                "-Xmn128M", "-XX:PermSize=64M", "-XX:MaxPermSize=128M", "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode", "-XX:-UseAdaptiveSizePolicy",
                "-cp", String.join(";", jars) + ";", cfg.required("mainClass").asText()));
        var game = WindowsCommandLine.splitArguments(cfg.required("minecraftArguments").asText(""));
        for (int i = 0; i < game.size(); i++) {
            String token = game.get(i);
            if ((token.equals("--userType") || token.equals("--versionType")) && i + 1 < game.size()) { i++; continue; }
            arguments.add(token.replace("${version_name}", version).replace("${assets_root}", minecraft("assets"))
                    .replace("${assets_index_name}", version).replace("${auth_uuid}", uuid)
                    .replace("${auth_access_token}", RandomUtil.getRandomString(32, "ABCDEF1234567890"))
                    .replace("${auth_player_name}", roleName).replace("${user_properties}", userProperties())
                    .replace("${game_directory}", workPath));
        }
    }
    private void buildModern(JsonNode cfg, int memory) {
        if (!cfg.hasNonNull("parameter_arguments") || !cfg.hasNonNull("jvm_arguments")) throw new IllegalArgumentException("Missing game/JVM arguments in game version JSON");
        arguments.addAll(List.of("-Xmx" + memory + "M", "-Xmn128M"));
        var jvm = WindowsCommandLine.splitArguments(cfg.path("jvm_arguments").asText());
        for (int i = 0; i < jvm.size(); i++) {
            String arg = jvm.get(i); if (arg.equals("-Xmx2G")) continue;
            if (arg.equals("-DlibraryDirectory=libraries")) arg = "-DlibraryDirectory=" + minecraft("libraries");
            arguments.add(arg);
            if ((arg.equals("-cp") || arg.equals("-p")) && i + 1 < jvm.size()) {
                String[] paths = jvm.get(++i).split(";", -1);
                arguments.add(String.join(";", Arrays.stream(paths).map(path -> minecraft(path)).toList()));
            }
        }
        var game = WindowsCommandLine.splitArguments(cfg.path("parameter_arguments").asText());
        for (int i = 0; i < game.size(); i++) {
            String token = game.get(i); arguments.add(token.replace("${auth_player_name}", roleName).replace("${auth_uuid}", uuid)
                    .replace("${auth_access_token}", gameVersion.value() >= EnumGameVersion.V_1_18.value() ? "0" : RandomUtil.getRandomString(32, "ABCDEF0123456789")));
            if (i + 1 < game.size() && token.equals("--assetsDir") && game.get(i + 1).equals("assets")) { arguments.add(minecraft("assets")); i++; }
            else if (i + 1 < game.size() && token.equals("--gameDir") && game.get(i + 1).equals(".")) { arguments.add(workPath); i++; }
        }
        arguments.addAll(List.of("--userProperties", userProperties()));
    }
    private String userPropertiesEx() {
        var properties = new EntityUserPropertiesEx(); properties.GameType = (int) EnumGType.NetGame.value(); properties.Channel = "netease";
        properties.TimeDelta = 0; properties.IsFilter = isFilter; properties.LauncherVersion = protocolVersion; return DotNetJson.serialize(properties);
    }
    private String userProperties() {
        boolean oldest = version.equals("1.7.10"); String filter = RandomUtil.getRandomString(32, "abcdefghijklmnopqrstuvwxyz");
        var values = new LinkedHashMap<String, Object>();
        values.put("uid", oldest ? List.of(new BigInteger(userId)) : List.of(new BigInteger(userId), 0));
        values.put("gameid", oldest ? List.of(0) : List.of(0, 0)); values.put("launcherport", oldest ? List.of(rpcPort) : List.of(rpcPort, 0));
        values.put("filterkey", List.of(filter, "0")); values.put("filterpath", List.of("", "0")); values.put("timedelta", List.of(0, 0));
        values.put("launchversion", List.of(oldest ? filter : protocolVersion, "0")); return DotNetJson.serializeRelaxed(values);
    }
}
