package codexus.gateway;

import codexus.game.launcher.utils.PathUtil;
import codexus.gateway.utils.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public final class Gateway {
    private static final System.Logger LOG = System.getLogger(Gateway.class.getName());
    private Gateway() { }
    public static void main(String[] arguments) throws Exception {
        var args = Set.copyOf(Arrays.asList(arguments));
        for (String arg : args) if (!arg.equals("--listen-all") && !arg.equals("--debug")) throw new IllegalArgumentException("Unknown Gateway argument: " + arg);
        try (var logging = GatewayLogging.configure(Path.of(PathUtil.BasePath, "logs"), args.contains("--debug"))) {
            if (PathUtil.containsChinese(PathUtil.BasePath)) throw new IllegalArgumentException("当前目录包含中文字符，请将应用程序移动到仅包含英文路径的目录中。");
            try (var runtime = GatewayRuntime.start(args.contains("--listen-all"))) {
                LOG.log(System.Logger.Level.INFO, "Gateway Java version: " + InfoUtil.AppVersion + InfoUtil.Postfix);
                LOG.log(System.Logger.Level.INFO, "访问 http://localhost:" + runtime.getHttpPort() + "?default=ws://localhost:" + runtime.getWebSocketPort() + "/gateway");
                var stopped = new CountDownLatch(1);
                Thread shutdown = Thread.ofPlatform().unstarted(() -> { try { runtime.close(); } finally { stopped.countDown(); } });
                Runtime.getRuntime().addShutdownHook(shutdown);
                // The upstream update endpoint distributes CLR binaries. Check and report its version;
                // never overwrite this JVM distribution (or java.exe) with the C# updater package.
                Thread.ofVirtual().name("gateway-version-check").start(() -> {
                    try {
                        if (!InfoUtil.isLatestVersion()) LOG.log(System.Logger.Level.WARNING,
                                "Upstream Gateway version " + InfoUtil.LatestVersion + " is available at " + InfoUtil.getDownloadUrl()
                                + "; this Java distribution requires a Java update package.");
                    } catch (Exception error) { LOG.log(System.Logger.Level.WARNING, "Unable to check upstream Gateway version", error); }
                });
                try { stopped.await(); }
                finally { try { Runtime.getRuntime().removeShutdownHook(shutdown); } catch (IllegalStateException ignored) { } }
            }
        }
    }
}
