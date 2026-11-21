package codexus.cipher.extensions;

import java.io.*;
import java.nio.*;
import java.util.concurrent.*;

public final class StreamExtensions {
    private StreamExtensions() { }
    public static CompletableFuture<ByteArrayInputStream> readSteamWithInt16Async(InputStream stream) {
        var result = new CompletableFuture<ByteArrayInputStream>();
        Thread.ofVirtual().start(() -> {
            try { result.complete(readSteamWithInt16(stream)); }
            catch (Exception e) { result.completeExceptionally(e); }
        });
        return result;
    }
    public static ByteArrayInputStream readSteamWithInt16(InputStream stream) throws IOException {
        byte[] prefix = stream.readNBytes(2);
        if (prefix.length != 2) throw new EOFException("Could not read the length prefix.");
        int length = ByteBuffer.wrap(prefix).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (length < 0) throw new IOException("Length cannot be negative.");
        var output = new ByteArrayOutputStream(length);
        byte[] buffer = new byte[1024];
        for (int remaining = length; remaining > 0;) {
            int count = stream.read(buffer, 0, Math.min(remaining, buffer.length));
            if (count < 0) throw new EOFException("End of stream reached before reading complete data.");
            if (count == 0) throw new EOFException("End of stream reached before reading complete data.");
            output.write(buffer, 0, count); remaining -= count;
        }
        return new ByteArrayInputStream(output.toByteArray());
    }
}
