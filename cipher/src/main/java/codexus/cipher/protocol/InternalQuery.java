package codexus.cipher.protocol;

import codexus.csharpsup.*;
import codexus.cipher.entities.mpay.whoami.*;
import codexus.cipher.utils.http.HttpWrapper;
import java.util.*;
import java.util.concurrent.*;

public final class InternalQuery {
    private static final HttpWrapper CLIENT = new HttpWrapper();
    private static volatile String gw = "";
    private static volatile EntityWhoAmi whoAmi = new EntityWhoAmi();
    private InternalQuery() { }
    public static String getGw() { return gw; }
    public static EntityWhoAmi getWhoAmi() { return whoAmi; }
    public static void initialize() {
        whoAmi = queryWhoAmi().join(); gw = queryGw().join();
    }
    public static String toAimInfo() {
        var location = DotNetJson.deserialize(DotNetEncoding.utf8(Base64.getDecoder().decode(whoAmi.Payload)), GeoLocationData.class);
        var aim = new EntityAimInfo(); aim.Code1 = location.Code1; aim.Code2 = location.Code2;
        aim.Code3 = location.Code3; aim.Code4 = location.Code4; aim.Isp = location.Isp; aim.Aim = location.Ip;
        return DotNetJson.serializeRelaxed(aim);
    }
    private static CompletableFuture<String> queryGw() {
        return CLIENT.getAsync("http://nstool.netease.com/internalquery", builder -> builder.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")).thenApply(response -> {
            HttpWrapper.ensureSuccess(response); Map<String, String> values = new HashMap<>();
            for (String line : HttpWrapper.readAsString(response).split("\\n", -1)) { String[] parts = line.split("=", -1); if (parts.length == 2) values.put(parts[0].strip(), parts[1].strip()); }
            return Objects.requireNonNull(values.get("gw"), "Missing gateway field");
        });
    }
    private static CompletableFuture<EntityWhoAmi> queryWhoAmi() {
        return CLIENT.getAsync("https://whoami.nie.netease.com/v6", builder -> {
            builder.addHeader("X-AUTH-PRODUCT", "g0"); builder.addHeader("X-AUTH-TOKEN", "token.efa8zUW6sxjR");
            builder.addHeader("X-IPDB-LOCALE", "en"); builder.addHeader("X-PROJECT_CODE", "x19");
        }).thenApply(response -> { HttpWrapper.ensureSuccess(response); return DotNetJson.deserialize(HttpWrapper.readAsString(response), EntityWhoAmi.class); });
    }
}
