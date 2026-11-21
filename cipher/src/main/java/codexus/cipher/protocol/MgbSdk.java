package codexus.cipher.protocol;

import codexus.csharpsup.*;
import codexus.cipher.entities.mgbsdk.EntityMgbSdkCookie;
import codexus.cipher.utils.StringGenerator;
import codexus.cipher.utils.http.HttpWrapper;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public final class MgbSdk implements AutoCloseable {
    private final String gameId;
    private final HttpWrapper sdk;
    private final Supplier<String> gateway, aimInfo;
    public MgbSdk(String gameId) { this(gameId, new HttpWrapper("https://mgbsdk.matrix.netease.com")); }
    public MgbSdk(String gameId, HttpWrapper sdk) { this(gameId, sdk, InternalQuery::getGw, InternalQuery::toAimInfo); }
    public MgbSdk(String gameId, HttpWrapper sdk, Supplier<String> gateway, Supplier<String> aimInfo) {
        this.gameId = Objects.requireNonNull(gameId); this.sdk = Objects.requireNonNull(sdk);
        this.gateway = Objects.requireNonNull(gateway); this.aimInfo = Objects.requireNonNull(aimInfo);
    }
    public String generateSAuth(String deviceId, String userId, String sdkUid, String sessionId, String timestamp, String channel) {
        return generateSAuth(deviceId, userId, sdkUid, sessionId, timestamp, channel, "pc");
    }
    public String generateSAuth(String deviceId, String userId, String sdkUid, String sessionId, String timestamp, String channel, String platform) {
        var cookie = new EntityMgbSdkCookie(); cookie.Ip = gateway.get(); cookie.AimInfo = aimInfo.get();
        cookie.AppChannel = channel; cookie.ClientLoginSn = deviceId; cookie.DeviceId = deviceId; cookie.GameId = gameId;
        cookie.LoginChannel = channel; cookie.SdkUid = sdkUid; cookie.SessionId = sessionId; cookie.Timestamp = timestamp;
        cookie.Platform = platform; cookie.SourcePlatform = platform; cookie.Udid = StringGenerator.generateHexString(16).toLowerCase(Locale.ROOT); cookie.UserId = userId;
        return DotNetJson.serializeRelaxed(cookie);
    }
    public CompletableFuture<Void> authSession(String cookie) {
        return sdk.postAsync("/" + gameId + "/sdk/uni_sauth", cookie).thenAccept(response -> {
            HttpWrapper.ensureSuccess(response); var value = DotNetJson.parse(HttpWrapper.readAsString(response));
            if (!value.path("code").asText().equals("200")) throw new HttpRequestException("Status: " + value.path("status").asText());
        });
    }
    @Override public void close() { sdk.close(); }
}
