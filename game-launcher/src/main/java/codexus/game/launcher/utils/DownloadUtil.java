package codexus.game.launcher.utils;

import codexus.csharpsup.CancellationToken;
import java.io.*;
import java.lang.foreign.Arena;
import java.net.URI;
import java.net.http.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.IntConsumer;

public final class DownloadUtil {
    private static final System.Logger LOG = System.getLogger(DownloadUtil.class.getName());
    private static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Duration TIMEOUT = Duration.ofMinutes(10);
    private static final int BUFFER_SIZE = 8192, SEGMENT_RETRIES = 3;
    private static final long MINIMUM_SEGMENTED_SIZE = 1024 * 1024;
    private DownloadUtil() { }
    public static CompletableFuture<Boolean> downloadAsync(String url, String destinationPath) {
        return downloadAsync(url, destinationPath, null, 8, CancellationToken.NONE);
    }
    public static CompletableFuture<Boolean> downloadAsync(String url, String destinationPath, IntConsumer progress) {
        return downloadAsync(url, destinationPath, progress, 8, CancellationToken.NONE);
    }
    public static CompletableFuture<Boolean> downloadAsync(String url, String destinationPath, IntConsumer progress,
                                                            int maxConcurrentSegments, CancellationToken cancellation) {
        Objects.requireNonNull(cancellation);
        return CompletableFuture.supplyAsync(() -> {
            try {
                cancellation.throwIfCancellationRequested();
                Path file = Path.of(destinationPath); Path parent = file.toAbsolutePath().getParent();
                if (parent != null) Files.createDirectories(parent);
                var head = request(url).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
                var response = send(head, HttpResponse.BodyHandlers.discarding(), cancellation);
                if (response.statusCode() == 405 || response.statusCode() == 501) return single(url, file, progress, cancellation);
                ensureSuccess(response);
                long length = response.headers().firstValueAsLong("Content-Length").orElse(-1);
                boolean supportsRanges = response.headers().allValues("Accept-Ranges").stream()
                        .flatMap(value -> Arrays.stream(value.split(","))).anyMatch(value -> value.strip().equalsIgnoreCase("bytes"));
                if (supportsRanges && maxConcurrentSegments >= 2 && length >= MINIMUM_SEGMENTED_SIZE) {
                    try { return segmented(url, file, length, maxConcurrentSegments, progress, cancellation); }
                    catch (RangeUnsupportedException e) { return single(url, file, progress, cancellation); }
                }
                return single(url, file, progress, cancellation);
            } catch (CancellationException e) { throw e; }
            catch (Exception e) {
                cancellation.throwIfCancellationRequested();
                LOG.log(System.Logger.Level.ERROR, "Download failed: " + url, e); return false;
            }
        }, command -> Thread.ofVirtual().name("codexus-download").start(command));
    }
    private static HttpRequest.Builder request(String url) { return HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT); }
    private static <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler, CancellationToken token) throws Exception {
        token.throwIfCancellationRequested();
        CompletableFuture<HttpResponse<T>> pending = HTTP.sendAsync(request, handler);
        try (var registration = token.register(() -> pending.cancel(true))) {
            try { return pending.get(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException(); }
            catch (ExecutionException e) { if (e.getCause() instanceof Exception cause) throw cause; throw e; }
        }
    }
    private static void ensureSuccess(HttpResponse<?> response) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IOException("HTTP " + response.statusCode());
    }
    private static boolean single(String url, Path file, IntConsumer callback, CancellationToken token) throws Exception {
        var response = send(request(url).GET().build(), HttpResponse.BodyHandlers.ofInputStream(), token);
        try (InputStream input = response.body(); var registration = token.register(() -> close(input))) {
            ensureSuccess(response);
            long total = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            Progress progress = new Progress(total, callback);
            try (var output = Files.newOutputStream(file)) {
                byte[] buffer = new byte[BUFFER_SIZE]; int count; long read = 0;
                while ((count = input.read(buffer)) != -1) {
                    token.throwIfCancellationRequested(); output.write(buffer, 0, count); read += count; progress.add(count);
                }
                token.throwIfCancellationRequested();
                if (total >= 0 && read != total) throw new EOFException("Incomplete download");
            }
            progress.complete(); return true;
        }
    }
    private record Range(long start, long end) { }
    private static boolean segmented(String url, Path file, long total, int concurrent, IntConsumer callback, CancellationToken token) throws Exception {
        int segments = Math.multiplyExact(concurrent, 3);
        List<Range> ranges = new ArrayList<>(); long segmentSize = total / segments;
        for (int i = 0; i < segments; i++) ranges.add(new Range(i * segmentSize, i == segments - 1 ? total - 1 : (i + 1) * segmentSize - 1));
        var semaphore = new Semaphore(concurrent); var progress = new Progress(total, callback);
        var errors = new ConcurrentLinkedQueue<Exception>();
        try (var allocation = new RandomAccessFile(file.toFile(), "rw")) { allocation.setLength(total); }
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var channel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            List<Future<?>> workers = new ArrayList<>();
            for (Range range : ranges) workers.add(executor.submit(() -> {
                boolean acquired = false;
                try {
                    token.throwIfCancellationRequested(); semaphore.acquire(); acquired = true;
                    for (int attempt = 1; ; attempt++) {
                        long received = 0;
                        try {
                            token.throwIfCancellationRequested();
                            var request = request(url).header("Range", "bytes=" + range.start + "-" + range.end).GET().build();
                            var response = send(request, HttpResponse.BodyHandlers.ofInputStream(), token);
                            try (InputStream input = response.body(); var registration = token.register(() -> close(input)); var arena = Arena.ofConfined()) {
                                if (response.statusCode() == 200) throw new RangeUnsupportedException();
                                ensureSuccess(response);
                                String expected = "bytes " + range.start + "-" + range.end + "/" + total;
                                if (response.statusCode() != 206 || !response.headers().firstValue("Content-Range").orElse("").equals(expected))
                                    throw new IOException("Invalid partial response for " + expected);
                                long length = range.end - range.start + 1;
                                var mapped = channel.map(FileChannel.MapMode.READ_WRITE, range.start, length, arena);
                                byte[] buffer = new byte[BUFFER_SIZE]; int count;
                                while ((count = input.read(buffer)) != -1) {
                                    token.throwIfCancellationRequested();
                                    if (received + count > length) throw new IOException("Range response exceeds requested size");
                                    mapped.asSlice(received, count).asByteBuffer().put(buffer, 0, count);
                                    received += count; progress.add(count);
                                }
                                if (received != length) throw new EOFException("Truncated range");
                                return;
                            }
                        } catch (Exception e) {
                            progress.subtract(received); token.throwIfCancellationRequested();
                            if (e instanceof CancellationException || e instanceof RangeUnsupportedException || attempt >= SEGMENT_RETRIES) throw e;
                            try (var registration = token.register(Thread.currentThread()::interrupt)) { Thread.sleep(500L * attempt); }
                        }
                    }
                } catch (Exception e) { errors.add(e); }
                finally { if (acquired) semaphore.release(); }
            }));
            for (Future<?> worker : workers) worker.get();
        }
        token.throwIfCancellationRequested();
        if (!errors.isEmpty()) {
            if (errors.stream().allMatch(RangeUnsupportedException.class::isInstance)) throw new RangeUnsupportedException();
            var failure = new IOException("Segmented download failed"); errors.forEach(failure::addSuppressed); throw failure;
        }
        progress.complete(); return true;
    }
    private static final class Progress {
        private final long total; private final IntConsumer callback;
        private long read, lastTime = System.nanoTime(); private int reported = -1;
        Progress(long total, IntConsumer callback) { this.total = total; this.callback = callback; }
        synchronized void add(long count) {
            read += count;
            if (total > 0 && System.nanoTime() - lastTime > 150_000_000L) {
                lastTime = System.nanoTime(); int percentage = Math.min(99, (int) (read * 100.0 / total));
                if (percentage > reported) { reported = percentage; if (callback != null) callback.accept(percentage); }
            }
        }
        synchronized void subtract(long count) { read -= count; }
        synchronized void complete() { reported = 100; if (callback != null) callback.accept(100); }
    }
    private static void close(InputStream input) { try { input.close(); } catch (IOException ignored) { } }
    private static final class RangeUnsupportedException extends IOException { }
}
