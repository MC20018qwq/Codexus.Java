package codexus.development.sdk.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public final class NetworkUtil {
    private NetworkUtil() { }
    public static int getAvailablePort() { return getAvailablePort(25565, 35565, false); }
    public static int getAvailablePort(int low) { return getAvailablePort(low, 35565, false); }
    public static int getAvailablePort(int low, int high) { return getAvailablePort(low, high, false); }
    public static int getAvailablePort(int low, int high, boolean reuseTimeWait) {
        if (low > high) return 0;
        Set<Integer> used = getUsedPorts(reuseTimeWait);
        for (int port = low; port <= high; port++) {
            if (!used.contains(port)) return port;
            if (port == Integer.MAX_VALUE) break;
        }
        return 0;
    }
    private static Set<Integer> getUsedPorts(boolean reuseTimeWait) {
        if (!System.getProperty("os.name").startsWith("Windows"))
            throw new UnsupportedOperationException("System port table support requires Windows.");
        Process process = null;
        try {
            Path executable = Path.of(System.getenv("SystemRoot"), "System32", "netstat.exe");
            process = new ProcessBuilder(executable.toString(), "-a", "-n").redirectErrorStream(true).start();
            Process readerProcess = process;
            var output = CompletableFuture.supplyAsync(() -> {
                try { return new String(readerProcess.getInputStream().readAllBytes(), StandardCharsets.US_ASCII); }
                catch (IOException e) { throw new UncheckedIOException(e); }
            });
            if (!process.waitFor(15, TimeUnit.SECONDS)) throw new IOException("Timed out reading Windows port tables");
            String text = output.get(5, TimeUnit.SECONDS);
            if (process.exitValue() != 0) throw new IOException("netstat failed: " + process.exitValue());
            return parsePortTable(text, reuseTimeWait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted reading Windows port tables", e);
        } catch (IOException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Cannot read Windows port tables", e);
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }
    static Set<Integer> parsePortTable(String text, boolean reuseTimeWait) {
        Set<Integer> result = new HashSet<>();
        for (String row : text.split("\\R")) {
            String[] fields = row.trim().split("\\s+");
            if (fields.length < 3 || !(fields[0].equals("TCP") || fields[0].equals("UDP"))) continue;
            if (fields[0].equals("TCP") && fields.length >= 4 && reuseTimeWait
                    && (fields[3].equals("TIME_WAIT") || fields[3].equals("CLOSE_WAIT"))) continue;
            String endpoint = fields[1];
            int colon = endpoint.lastIndexOf(':');
            if (colon < 0) throw new IllegalStateException("Invalid local endpoint in Windows port table");
            result.add(Integer.parseInt(endpoint.substring(colon + 1)));
        }
        return result;
    }
}
