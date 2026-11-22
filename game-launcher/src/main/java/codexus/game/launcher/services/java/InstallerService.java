package codexus.game.launcher.services.java;

import codexus.cipher.entities.*;
import codexus.cipher.entities.wpflauncher.minecraft.*;
import codexus.cipher.entities.wpflauncher.netgame.*;
import codexus.cipher.entities.wpflauncher.netgame.mods.*;
import codexus.cipher.entities.wpflauncher.netgame.texture.*;
import codexus.cipher.protocol.WPFLauncher;
import codexus.cipher.game.GameArtifactResolver;
import codexus.cipher.game.GameAuthenticationMd5;
import codexus.csharpsup.DotNetJson;
import codexus.game.launcher.utils.*;
import codexus.game.launcher.utils.progress.SyncProgressBarUtil.ProgressBar;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public final class InstallerService {
    private static final System.Logger LOG = System.getLogger(InstallerService.class.getName());
    private static final Object CLIENT_INSTALL_LOCK = new Object();
    private InstallerService() { }
    private static <T> CompletableFuture<T> async(Callable<T> work) {
        return CompletableFuture.supplyAsync(() -> { try { return work.call(); } catch (IOException e) { throw new UncheckedIOException(e); } catch (RuntimeException e) { throw e; } catch (Exception e) { throw new CompletionException(e); } }, command -> Thread.ofVirtual().name("codexus-install").start(command));
    }
    public static CompletableFuture<Boolean> prepareMinecraftClient(String userId, String userToken, WPFLauncher wpf, EnumGameVersion gameVersion) {
        return async(() -> {
            synchronized (CLIENT_INSTALL_LOCK) {
                var base = wpf.getMinecraftClientLibs(userId, userToken);
                if (base.Code != 0 || base.Data == null) throw new IllegalStateException("Failed to fetch base package: " + base.Message);
                processPackage(base.Data.Url, Path.of(PathUtil.CachePath, "GameBase.7z"), Path.of(PathUtil.GameBasePath), Path.of(PathUtil.GameBasePath, "GAME_BASE.MD5"), base.Data.Md5, "base package", false);
                prepareVersionFiles(userId, userToken, wpf, gameVersion);
                return true;
            }
        });
    }
    /** Prepares only the authentication inputs selected by the shared MD5 switch. */
    public static CompletableFuture<Void> prepareAuthentication(String userId, String userToken, WPFLauncher wpf, EnumGameVersion gameVersion) {
        return prepareAuthentication(userId, userToken, wpf, gameVersion, GameAuthenticationMd5.DYNAMIC_DOWNLOAD);
    }

    static CompletableFuture<Void> prepareAuthentication(String userId, String userToken, WPFLauncher wpf, EnumGameVersion gameVersion, boolean dynamicDownload) {
        if (dynamicDownload) {
            return prepareAuthenticationFiles(userId, userToken, wpf, gameVersion).thenApply(files -> null);
        }
        try {
            // No package API, filesystem hashing, or downloads in hardcoded mode.
            GameAuthenticationMd5.hardcoded(GameVersionUtil.getGameVersionFromEnum(gameVersion));
            return CompletableFuture.completedFuture(null);
        } catch (RuntimeException error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    /** Proxy authentication needs the version data and modified loader, but no Java runtime or shared game assets. */
    public static CompletableFuture<GameArtifactResolver.GameArtifacts> prepareAuthenticationFiles(String userId, String userToken, WPFLauncher wpf, EnumGameVersion gameVersion) {
        return async(() -> {
            synchronized (CLIENT_INSTALL_LOCK) {
                return prepareVersionFiles(userId, userToken, wpf, gameVersion);
            }
        }).whenComplete((files, error) -> {
            if (error != null) LOG.log(System.Logger.Level.ERROR, "Failed to prepare NetEase authentication files for " + gameVersion, error);
        });
    }
    private static GameArtifactResolver.GameArtifacts prepareVersionFiles(String userId, String userToken, WPFLauncher wpf, EnumGameVersion gameVersion) throws IOException {
        String versionName = GameVersionUtil.getGameVersionFromEnum(gameVersion);
        if (!versionName.startsWith("1.")) throw new IllegalArgumentException("Unsupported Java game version: " + versionName);
        String name = gameVersion.name();
        LOG.log(System.Logger.Level.INFO, "Preparing NetEase authentication files for Minecraft " + versionName);
        var result = wpf.getMinecraftClientLibs(userId, userToken, gameVersion);
        if (result.Code != 0 || result.Data == null) throw new IllegalStateException("Failed to fetch " + name + " package: " + result.Message);
        var version = result.Data;
        var resolver = new GameArtifactResolver(Path.of(PathUtil.GameBasePath, ".minecraft"));
        boolean repair = false;
        try { resolver.resolve(versionName); }
        catch (UncheckedIOException missing) {
            repair = true;
            LOG.log(System.Logger.Level.INFO, "Installing or repairing NetEase authentication files: " + missing.getMessage());
        }
        // A matching archive marker alone cannot prove that its installed files still exist.
        boolean versionChanged = processPackage(version.Url, Path.of(PathUtil.CachePath, name + ".7z"), Path.of(PathUtil.GameBasePath),
                Path.of(PathUtil.GameBasePath, name + ".MD5"), version.Md5, name + " package", repair);
        Path libraryMarker = Path.of(PathUtil.GameBasePath, name + "_Lib.MD5");
        String fingerprint = version.CoreLibMd5 + ";layout=2";
        if (repair || versionChanged || !Files.isRegularFile(libraryMarker) || !Files.readString(libraryMarker).equals(fingerprint)) {
            Path staging = Path.of(PathUtil.CachePath, name + "_core_stage"), archive = Path.of(PathUtil.CachePath, name + "_Lib.7z");
            if (!FileUtil.deleteDirectorySafe(staging.toString())) throw new IOException("Cannot clear core library staging directory");
            try (var progress = new ProgressBar(100)) {
                downloadAndExtract(version.CoreLibUrl, archive, staging, version.CoreLibMd5, progress, name + " libraries");
                installCoreLibs(staging, gameVersion);
                resolver.resolve(versionName);
                Files.writeString(libraryMarker, fingerprint); Files.deleteIfExists(archive);
            }
        }
        var files = resolver.resolve(versionName);
        LOG.log(System.Logger.Level.INFO, "NetEase authentication files ready for Minecraft " + versionName + ": " + files.datFile() + ", " + files.bootstrapJar());
        return files;
    }
    private static boolean processPackage(String url, Path archive, Path output, Path marker, String md5, String label, boolean force) throws IOException {
        if (!force && Files.isRegularFile(marker) && Files.readString(marker).equals(md5)) return false;
        try (var progress = new ProgressBar(100)) {
            downloadAndExtract(url, archive, output, md5, progress, label);
            Files.createDirectories(marker.getParent()); Files.writeString(marker, md5); Files.deleteIfExists(archive);
        }
        return true;
    }
    private static void downloadAndExtract(String url, Path archive, Path output, String md5, ProgressBar progress, String label) throws IOException {
        if (!DownloadUtil.downloadAsync(url, archive.toString(), percent -> progress.update(percent, "Downloading " + label)).join()) throw new IOException("Failed to download " + label);
        if (md5 != null && !md5.isBlank() && !FileUtil.computeMd5FromFile(archive.toString()).equalsIgnoreCase(md5)) throw new IOException("MD5 mismatch for " + label);
        if (!CompressionUtil.extract7Z(archive.toString(), output.toString(), percent -> progress.update(percent, "Extracting " + label))) throw new IOException("Failed to extract " + label);
    }
    private static void copy(Path source, Path destination) throws IOException { Files.createDirectories(destination.getParent()); Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING); }
    static void installCoreLibs(Path directory, EnumGameVersion gameVersion) throws IOException {
        if (!Files.isDirectory(directory)) throw new IOException("Missing extracted core libraries: " + directory);
        String version = GameVersionUtil.getGameVersionFromEnum(gameVersion);
        Path root = Path.of(PathUtil.GameBasePath, ".minecraft"), versions = root.resolve(Path.of("versions", version));
        String[] files = FileUtil.enumerateFiles(directory.toString());
        // Install the version JSON first: its library coordinates determine the destination paths.
        for (String name : files) {
            Path source = Path.of(name); String file = source.getFileName().toString();
            if (file.equals(version + ".jar") || file.equals(version + ".json") || file.equals(version + ".dat")) copy(source, versions.resolve(file));
        }
        var destinations = new HashMap<String, Set<Path>>();
        Path config = versions.resolve(version + ".json"), libraries = root.resolve("libraries").toAbsolutePath().normalize();
        if (Files.isRegularFile(config)) {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_TRAILING_COMMA);
            var cfg = mapper.readTree(Files.readAllBytes(config));
            // NetEase can declare modified loaders only in the runtime classpath, not in libraries[].
            for (Path target : new GameArtifactResolver(root).declaredRuntimeJars(version)) {
                if (target.startsWith(libraries))
                    destinations.computeIfAbsent(target.getFileName().toString(), ignored -> new LinkedHashSet<>()).add(target);
            }
            if (cfg != null) for (var library : cfg.path("libraries")) {
                String relative = library.path("downloads").path("artifact").path("path").asText("");
                if (relative.isEmpty()) {
                    String[] coordinate = library.path("name").asText("").split(":", -1);
                    if (coordinate.length < 3) continue;
                    String file = coordinate[1] + "-" + coordinate[2] + (coordinate.length > 3 ? "-" + coordinate[3] : "") + ".jar";
                    relative = coordinate[0].replace('.', '/') + "/" + coordinate[1] + "/" + coordinate[2] + "/" + file;
                }
                Path target = libraries.resolve(relative.replace('\\', '/')).normalize();
                if (!target.startsWith(libraries)) throw new IOException("Library path escapes library directory: " + relative);
                destinations.computeIfAbsent(target.getFileName().toString(), ignored -> new LinkedHashSet<>()).add(target);
            }
        }
        for (String name : files) {
            Path source = Path.of(name); String file = source.getFileName().toString();
            if (file.equals(version + ".jar") || file.equals(version + ".json") || file.equals(version + ".dat")) continue;
            var targets = destinations.get(file);
            if (targets != null) { for (Path destination : targets) copy(source, destination); continue; }
            String library = file.contains(".") ? file.substring(0, file.lastIndexOf('.')) : file;
            Path destination = null;
            if (file.startsWith("forge-" + version + "-")) destination = libraries.resolve(Path.of("net", "minecraftforge", "forge", library.substring(6), library + ".jar"));
            else if (file.startsWith("launchwrapper-")) destination = libraries.resolve(Path.of("net", "minecraft", "launchwrapper", library.substring(14), library + ".jar"));
            else if (file.startsWith("MercuriusUpdater-")) destination = libraries.resolve(Path.of("net", "minecraftforge", "MercuriusUpdater", library.substring(17), library + ".jar"));
            if (destination != null) copy(source, destination);
            else if (file.endsWith(".jar")) throw new IOException("Core library is not declared by the game version JSON: " + file);
        }
        if (!FileUtil.deleteDirectorySafe(directory.toString())) throw new IOException("Failed to delete extracted libraries: " + directory);
    }
    public record InstalledGameMods(EntityModsList mods, int requiredJavaVersion) { }

    /** NetEase aov builds a table by mc_version_name; it never chooses an unrelated first entry. */
    static EntityComponentDownloadInfoResponseSub selectResource(EntityComponentDownloadInfoResponse item, EnumGameVersion version) {
        String name = GameVersionUtil.getGameVersionFromEnum(version);
        if (item != null && item.SubEntities != null)
            for (var sub : item.SubEntities)
                if (sub != null && name.equals(sub.McVersionName)) return sub;
        throw new IllegalStateException("No resource for Minecraft " + name + " in component " + (item == null ? "null" : item.ItemId));
    }

    public static CompletableFuture<EntityModsList> installGameMods(String userId, String userToken, EnumGameVersion version, WPFLauncher wpf, String gameId, boolean isRental) {
        return installGameModsWithRuntime(userId, userToken, version, wpf, gameId, isRental).thenApply(InstalledGameMods::mods);
    }

    public static CompletableFuture<InstalledGameMods> installGameModsWithRuntime(String userId, String userToken, EnumGameVersion version, WPFLauncher wpf, String gameId, boolean isRental) {
        return async(() -> {
            int componentJava = 0;
            var result = wpf.getGameCoreModListAsync(userId, userToken, version, isRental).join();
            if (result == null || result.Code != 0 || result.Data == null || result.Data.IidList == null)
                throw new IllegalStateException("Failed to fetch core mod list");
            var resources = new LinkedHashMap<EntityComponentDownloadInfoResponse, EntityComponentDownloadInfoResponseSub>();
            if (!result.Data.IidList.isEmpty()) {
                var details = wpf.getGameCoreModDetailsListAsync(userId, userToken, result.Data.IidList).join();
                if (details == null || details.Code != 0 || details.Data == null) throw new IllegalStateException("Failed to fetch core mod details");
                for (var item : details.Data) {
                    var resource = selectResource(item, version);
                    resources.put(item, resource); componentJava = Math.max(componentJava, resource.JavaVersion);
                }
            }
            var mods = new EntityModsList();
            try (var progress = new ProgressBar(100)) {
                Path core = Path.of(PathUtil.GameModsPath, gameId);
                if (!FileUtil.deleteDirectorySafe(core.toString())) throw new IOException("Cannot clear core mods: " + core);
                int index = 0;
                for (var entry : resources.entrySet()) {
                    index++; var item = entry.getKey(); var resource = entry.getValue();
                    String filename = Path.of(resource.ResName).getFileName().toString();
                    String name = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
                    Path jar = core.resolve(name + "@" + item.MTypeId + "@" + item.EntityId + ".jar"), archive = core.resolve(filename), extracted = core.resolve(name);
                    downloadAndExtract(resource.ResUrl, archive, extracted, resource.ResMd5, progress, "core mod " + index + "/" + resources.size());
                    String matched = null;
                    for (String candidate : FileUtil.enumerateFiles(extracted.toString(), "jar"))
                        if (FileUtil.computeMd5FromFile(candidate).equalsIgnoreCase(resource.JarMd5)) { matched = candidate; break; }
                    if (matched == null) throw new IOException("Core mod archive contains no matching JAR: " + resource.ResName);
                    copy(Path.of(matched), jar);
                    FileUtil.deleteDirectorySafe(extracted.toString()); Files.deleteIfExists(archive);
                    var mod = new EntityModsInfo(); mod.ModPath = jar.getFileName().toString(); mod.Id = mod.ModPath;
                    mod.Iid = item.ItemId; mod.Md5 = resource.JarMd5.toUpperCase(Locale.ROOT); mods.Mods.add(mod);
                }
                progress.update(100, "Core mods ready");
                Path componentDir = Path.of(PathUtil.CachePath, "Game", gameId), archive = Path.of(componentDir + ".7z");
                Files.createDirectories(componentDir);
                var componentResponse = wpf.getNetGameComponentDownloadList(userId, userToken, gameId);
                if (componentResponse != null && componentResponse.Code != 0) throw new IllegalStateException("Failed to fetch game assets: " + componentResponse.Message);
                if (componentResponse != null && componentResponse.Data != null) {
                    var component = selectResource(componentResponse.Data, version);
                    componentJava = Math.max(componentJava, component.JavaVersion);
                    Path marker = componentDir.resolve(gameId + ".MD5"), manifest = componentDir.resolve(gameId + ".json");
                    String fingerprint = version.xmlName() + "\n" + component.ResMd5;
                    boolean needsUpdate = !Files.isRegularFile(marker) || !Files.readString(marker).equals(fingerprint);
                    if (!needsUpdate && Files.isRegularFile(manifest)) {
                        var cached = DotNetJson.deserialize(Files.readString(manifest), EntityModsList.class);
                        if (cached != null) mods.Mods.addAll(cached.Mods);
                    } else {
                        Files.deleteIfExists(archive);
                        if (!DownloadUtil.downloadAsync(component.ResUrl, archive.toString(), percent -> progress.update(percent, "Downloading game assets")).join()) throw new IOException("Game assets download failed");
                        if (!FileUtil.computeMd5FromFile(archive.toString()).equalsIgnoreCase(component.ResMd5)) throw new IOException("Game assets MD5 mismatch");
                        if (!FileUtil.deleteDirectorySafe(componentDir.toString())) throw new IOException("Cannot clear game asset directory");
                        if (!CompressionUtil.extract7Z(archive.toString(), componentDir.toString(), percent -> progress.update(percent, "Extracting game assets"))) throw new IOException("Game assets extraction failed");
                        var serverMods = new EntityModsList();
                        for (String file : FileUtil.enumerateFiles(componentDir.resolve(Path.of(".minecraft", "mods")).toString(), "jar")) {
                            String jarName = Path.of(file).getFileName().toString(); var mod = new EntityModsInfo();
                            mod.ModPath = jarName; mod.Id = jarName; mod.Iid = jarName.split("@", -1)[0]; mod.Md5 = FileUtil.computeMd5FromFile(file).toUpperCase(Locale.ROOT); serverMods.Mods.add(mod);
                        }
                        mods.Mods.addAll(serverMods.Mods); Files.writeString(manifest, DotNetJson.serialize(serverMods)); Files.writeString(marker, fingerprint); Files.deleteIfExists(archive);
                    }
                }
                progress.update(100, "Game assets ready"); ProgressBar.clearCurrent();
                return new InstalledGameMods(mods, JavaRuntimeUtil.requiredVersion(version, componentJava, isRental));
            }
        });
    }
    public static String prepareGameRuntime(String userId, String gameId, String roleName, EnumGType gameType) {
        Path runtime = Path.of(PathUtil.GamePath, "Runtime", HashUtil.generateGameRuntimeId(gameId, roleName)), minecraft = runtime.resolve(".minecraft");
        try {
            Files.createDirectories(runtime);
            if (gameType == EnumGType.NetGame) {
                Path mods = minecraft.resolve("mods"); FileUtil.deleteDirectorySafe(mods.toString()); Files.createDirectories(mods);
                FileUtil.copyDirectory(Path.of(PathUtil.CachePath, "Game", gameId, ".minecraft").toString(), minecraft.toString(), false, false);
                for (String custom : FileUtil.enumerateFiles(PathUtil.CustomModsPath, "jar")) copy(Path.of(custom), mods.resolve(Path.of(custom).getFileName()));
            }
            Path assets = minecraft.resolve("assets"); FileUtil.deleteDirectorySafe(assets.toString()); Files.createDirectories(minecraft);
            if (!FileUtil.createSymbolicLinkSafe(assets.toString(), Path.of(PathUtil.GameBasePath, ".minecraft", "assets").toString())) throw new IOException("Cannot create shared assets link: " + assets);
            return runtime.toString();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static void installCoreMods(String gameId, String targetModsPath) {
        Path source = Path.of(PathUtil.GameModsPath, gameId), target = Path.of(targetModsPath);
        try { for (String file : FileUtil.enumerateFiles(source.toString())) copy(Path.of(file), target.resolve(source.relativize(Path.of(file)))); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static void installNativeDll(EnumGameVersion version) {
        try { copy(Path.of(PathUtil.ResourcePath, "api-ms-win-crt-utility-l1-1-1.dll"), Path.of(PathUtil.GameBasePath, ".minecraft", "versions", GameVersionUtil.getGameVersionFromEnum(version), "natives", "runtime", "api-ms-win-crt-utility-l1-1-1.dll")); }
        catch (Exception e) { System.getLogger(InstallerService.class.getName()).log(System.Logger.Level.ERROR, "Failed to install native dll", e); }
    }
}
