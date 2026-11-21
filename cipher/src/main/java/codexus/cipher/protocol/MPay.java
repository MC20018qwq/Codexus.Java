package codexus.cipher.protocol;

import codexus.csharpsup.*;
import codexus.cipher.entities.mpay.*;
import codexus.cipher.extensions.MPayExtensions;
import codexus.cipher.utils.StringGenerator;
import codexus.cipher.utils.exception.VerifyException;
import codexus.cipher.utils.http.*;
import java.io.*;
import java.nio.file.*;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;

public final class MPay implements AutoCloseable {
    private final EntityDevice device;
    private final HttpWrapper client, service;
    private final String gameId;
    public final String Unique, GameVersion;
    public MPay(String gameId, String gameVersion) {
        this.gameId = gameId; GameVersion = gameVersion;
        client = new HttpWrapper(); service = new HttpWrapper("https://service.mkey.163.com");
        Unique = createOrLoadUnique(gameId); device = createOrLoadDeviceAsync(gameId).join();
    }
    /** Allows an embedding application to supply an already registered device and transport. */
    public MPay(String gameId, String gameVersion, String unique, EntityDevice device, HttpWrapper client, HttpWrapper service) {
        this.gameId = gameId; GameVersion = gameVersion; Unique = unique; this.device = device; this.client = client; this.service = service;
    }
    @Override public void close() { client.close(); service.close(); }
    public EntityDevice getDevice() { return device; }
    public CompletableFuture<Void> configure(String appKey, String channel) {
        String query = new ParameterBuilder().append("sub_app_key", "").append("api_ver", "2").append("gdpr", "0")
                .append("app_channel", channel).append("sdk_version", "c1.0.0").append("app_key", appKey)
                .append("device_id", Unique.toUpperCase(Locale.ROOT)).formUrlEncode();
        return client.getAsync("https://analytics.mpay.netease.com/config?" + query).thenAccept(HttpWrapper::ensureSuccess);
    }
    private static String createOrLoadUnique(String gameId) {
        String file = gameId + "-guid.cds"; byte[] stored = loadFromFile(file);
        if (stored != null) return DotNetEncoding.utf8(stored);
        String unique = UUID.randomUUID().toString().replace("-", ""); saveToFile(file, unique); return unique;
    }
    private CompletableFuture<EntityDevice> createOrLoadDeviceAsync(String gameId) {
        String file = gameId + "-device.cds"; byte[] stored = loadFromFile(file);
        if (stored != null) return CompletableFuture.completedFuture(DotNetJson.deserialize(DotNetEncoding.utf8(stored), EntityDeviceResponse.class).EntityDevice);
        return service.postAsync("/mpay/games/" + gameId + "/devices", buildDeviceParams().append("unique_id", Unique).formUrlEncode(), "application/x-www-form-urlencoded")
                .thenApply(response -> { HttpWrapper.ensureSuccess(response); String json = HttpWrapper.readAsString(response); saveToFile(file, json); return DotNetJson.deserialize(json, EntityDeviceResponse.class).EntityDevice; });
    }
    public CompletableFuture<EntityMPayUserResponse> loginWithEmailAsync(String email, String password) {
        var parameters = new EntityUsersParameters(); parameters.Password = MPayExtensions.encodeMd5(password); parameters.Unique = Unique; parameters.Username = email;
        String encrypted = MPayExtensions.encodeHex(MPayExtensions.encodeAes(DotNetJson.serializeRelaxed(parameters), MPayExtensions.decodeHex(device.Key)));
        return service.postAsync("/mpay/games/" + gameId + "/devices/" + device.Id + "/users", buildBaseParams()
                .append("opt_fields", "nickname,avatar,realname_status,mobile_bind_status,mask_related_mobile,related_login_status")
                .append("params", encrypted).append("un", MPayExtensions.encodeBase64(email)).formUrlEncode(), "application/x-www-form-urlencoded")
                .thenApply(response -> {
                    String json = HttpWrapper.readAsString(response);
                    if (!success(response)) {
                        var verify = DotNetJson.deserialize(json, EntityVerifyResponse.class);
                        if (verify != null && verify.Code == 1351) throw new VerifyException(json);
                        throw new IllegalStateException("Failed to login with email, response: " + json);
                    }
                    return DotNetJson.deserialize(json, EntityMPayUserResponse.class);
                });
    }
    public CompletableFuture<Boolean> sendSmsCodeAsync(String phoneNumber) {
        return service.postAsync("/mpay/api/users/login/mobile/get_sms", buildBaseParams().append("device_id", device.Id).append("mobile", phoneNumber).formUrlEncode(), "application/x-www-form-urlencoded")
                .thenApply(response -> { if (!success(response)) logFailure("send sms code", response); return success(response); });
    }
    public CompletableFuture<EntitySmsTicket> verifySmsCodeAsync(String phoneNumber, String code) {
        return service.postAsync("/mpay/api/users/login/mobile/verify_sms", buildBaseParams().append("device_id", device.Id).append("mobile", phoneNumber)
                .append("smscode", code).append("up_content", "").formUrlEncode(), "application/x-www-form-urlencoded")
                .thenApply(response -> { if (success(response)) return DotNetJson.deserialize(HttpWrapper.readAsString(response), EntitySmsTicket.class); logFailure("send sms code", response); return null; });
    }
    public CompletableFuture<EntityMPayUserResponse> finishSmsCodeAsync(String phoneNumber, String ticket) {
        return service.postAsync("/mpay/api/users/login/mobile/finish?un=" + MPayExtensions.encodeBase64(phoneNumber), buildBaseParams().append("device_id", device.Id)
                .append("opt_fields", "nickname,avatar,realname_status,mobile_bind_status,mask_related_mobile,related_login_status").append("ticket", ticket).formUrlEncode(), "application/x-www-form-urlencoded")
                .thenApply(response -> { if (success(response)) return DotNetJson.deserialize(HttpWrapper.readAsString(response), EntityMPayUserResponse.class); logFailure("finish sms code", response); return null; });
    }
    private ParameterBuilder buildBaseParams() {
        return new ParameterBuilder().append("app_channel", "netease").append("app_mode", "2").append("app_type", "games")
                .append("arch", "win_x64").append("cv", "c4.2.0").append("mcount_app_key", "EEkEEXLymcNjM42yLY3Bn6AO15aGy4yq")
                .append("mcount_transaction_id", "0").append("process_id", Long.toString(ProcessHandle.current().pid()))
                .append("sv", "10.0.22621").append("updater_cv", "c1.0.0").append("game_id", gameId).append("gv", GameVersion);
    }
    private ParameterBuilder buildDeviceParams() {
        return buildBaseParams().append("brand", "Microsoft").append("device_model", "pc_mode").append("device_name", "PC-" + StringGenerator.generateRandomString(12))
                .append("device_type", "Computer").append("init_urs_device", "0").append("mac", StringGenerator.generateRandomMacAddress())
                .append("resolution", "1920x1080").append("system_name", "windows").append("system_version", "10.0.22621");
    }
    private static boolean success(HttpResponse<?> response) { return response.statusCode() >= 200 && response.statusCode() < 300; }
    private static void logFailure(String action, HttpResponse<byte[]> response) { System.getLogger(MPay.class.getName()).log(System.Logger.Level.ERROR, "Failed to " + action + ", response: " + HttpWrapper.readAsString(response)); }
    public static void saveToFile(String filename, String content) {
        try { Files.write(Path.of(filename), DotNetEncoding.utf8(content)); } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static byte[] loadFromFile(String filename) {
        try { return Files.readAllBytes(Path.of(filename)); } catch (IOException e) { return null; }
    }
}
