package codexus.gateway.utils;

import codexus.csharpsup.DotNetJson;
import codexus.development.sdk.utils.WebNexusApi;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class InfoUtil {
    public static final String AppVersion = "2.54.84912";
    public static final String Postfix = "-beta.3+20260806";
    private static final String AppId = "Codexus.Gateway", AppSecret = "a7K9m2XqL8bC4wZ1";
    public static final String InitToken = "YXBwSWQ9Q29kZXh1cy5HYXRld2F5LmFwcFNlY3JldD1hN0s5bTJYcUw4YkM0d1ox";
    public static volatile String LatestVersion = AppVersion;
    private static volatile String downloadUrl = "https://www.codexus.today/download";
    private InfoUtil() { }
    public static String getDownloadUrl() { return downloadUrl; }
    public static boolean isLatestVersion() {
        try (var api = new WebNexusApi(InitToken)) {
            var response = DotNetJson.parse(api.getAppVersionAsync(AppId, AppSecret).orTimeout(20, TimeUnit.SECONDS).join());
            String latest = response == null ? "" : response.path("version").asText("");
            if (latest.isBlank()) throw new IllegalStateException("Empty version from server");
            boolean current = compareVersions(AppVersion, latest) >= 0;
            if (!current) {
                var download = DotNetJson.parse(api.getGatewayDownloadUrlAsync(getSystemIdentifier()).orTimeout(20, TimeUnit.SECONDS).join());
                String url = download == null ? "" : download.path("url").asText("");
                if (url.isBlank()) throw new IllegalStateException("Empty Gateway download URL from server");
                downloadUrl = url;
            }
            LatestVersion = latest; return current;
        }
    }
    public static int compareVersions(String first, String second) {
        int[] a = parseVersion(first), b = parseVersion(second);
        for (int i = 0; i < 4; i++) if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
        return 0;
    }
    private static int[] parseVersion(String value) {
        String[] parts = value.split("\\.", -1);
        if (parts.length < 2 || parts.length > 4) throw new IllegalArgumentException("Invalid application version: " + value);
        int[] result = {-1, -1, -1, -1};
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].matches("[0-9]+")) throw new IllegalArgumentException("Invalid application version: " + value);
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }
    public static String getSystemIdentifier() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT), architecture = System.getProperty("os.arch");
        String platform = os.startsWith("windows") ? "windows" : os.startsWith("mac") ? "macos" : os.startsWith("linux") ? "linux" : null;
        if (platform == null) return "null";
        return platform + (architecture.equalsIgnoreCase("aarch64") || architecture.equalsIgnoreCase("arm64") ? "_arm64" : "_x64");
    }
}
