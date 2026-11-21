package codexus.gateway.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.logging.*;

/** Console output and hourly UTF-8 log files, with the original 31-file / 1 GiB bounds. */
public final class GatewayLogging extends Handler implements AutoCloseable {
    private final Path directory;
    private BufferedWriter output;
    private String hour;
    private long written;
    private int sequence;
    private GatewayLogging(Path directory) throws IOException { this.directory = directory; Files.createDirectories(directory); }
    public static GatewayLogging configure(Path directory, boolean debug) throws IOException {
        var root = Logger.getLogger("");
        for (var handler : root.getHandlers()) { root.removeHandler(handler); handler.close(); }
        root.setLevel(debug ? Level.FINE : Level.INFO);
        var console = new ConsoleHandler(); console.setLevel(root.getLevel()); console.setFormatter(new LineFormatter()); root.addHandler(console);
        var file = new GatewayLogging(directory); file.setLevel(Level.ALL); file.setFormatter(new LineFormatter()); root.addHandler(file);
        return file;
    }
    @Override public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) return;
        try {
            String currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH"));
            String text = getFormatter().format(record); int bytes = text.getBytes(StandardCharsets.UTF_8).length;
            if (output == null || !currentHour.equals(hour) || written + bytes > 1073741824L) {
                close();
                if (!currentHour.equals(hour)) sequence = 0;
                hour = currentHour;
                Path path;
                do { path = directory.resolve("codexus-gateway-java-" + hour + "-" + sequence++ + ".log"); }
                while (Files.exists(path) && Files.size(path) >= 1073741824L);
                written = Files.exists(path) ? Files.size(path) : 0;
                output = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                try (var files = Files.list(directory)) {
                    var old = files.filter(p -> p.getFileName().toString().matches("codexus-gateway-java-[0-9]{8}-[0-9]{2}-[0-9]+\\.log"))
                            .sorted(Comparator.comparingLong(GatewayLogging::modified).reversed()).skip(31).toList();
                    for (var file : old) if (!file.equals(path)) Files.deleteIfExists(file);
                }
            }
            output.write(text); output.flush(); written += bytes;
        } catch (IOException e) { reportError("Unable to write Gateway log", e, ErrorManager.WRITE_FAILURE); }
    }
    private static long modified(Path path) { try { return Files.getLastModifiedTime(path).toMillis(); } catch (IOException e) { return 0; } }
    @Override public synchronized void flush() { try { if (output != null) output.flush(); } catch (IOException e) { reportError("Unable to flush Gateway log", e, ErrorManager.FLUSH_FAILURE); } }
    @Override public synchronized void close() { try { if (output != null) output.close(); } catch (IOException e) { reportError("Unable to close Gateway log", e, ErrorManager.CLOSE_FAILURE); } finally { output = null; } }
    private static final class LineFormatter extends java.util.logging.Formatter {
        @Override public String format(LogRecord record) {
            String line = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS XXX").format(record.getInstant().atZone(ZoneId.systemDefault()))
                    + " [" + record.getLevel().getName() + "] " + formatMessage(record) + System.lineSeparator();
            if (record.getThrown() == null) return line;
            var trace = new StringWriter(); record.getThrown().printStackTrace(new PrintWriter(trace)); return line + trace;
        }
    }
}
