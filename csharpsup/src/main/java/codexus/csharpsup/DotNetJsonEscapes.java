package codexus.csharpsup;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import java.io.*;
import java.util.*;

/** Unicode escaping tables captured from the original System.Text.Encodings.Web implementation. */
final class DotNetJsonEscapes extends CharacterEscapes {
    private final byte[] blocked;
    private final int[] ascii = CharacterEscapes.standardAsciiEscapesForJSON();
    DotNetJsonEscapes(boolean relaxed) {
        String resource = relaxed ? "json-escape-relaxed.bin" : "json-escape-default.bin";
        try (InputStream input = DotNetJsonEscapes.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Missing JSON escaping data: " + resource);
            blocked = input.readAllBytes();
            if (blocked.length != 8192) throw new IllegalStateException("Invalid JSON escaping data: " + resource);
        } catch (IOException e) { throw new UncheckedIOException(e); }
        for (int ch = 0; ch < ascii.length; ch++) if (isBlocked(ch) && (ascii[ch] == 0 || (!relaxed && ch == '"'))) ascii[ch] = ESCAPE_CUSTOM;
    }
    private boolean isBlocked(int ch) { return ch > 65535 || (blocked[ch >> 3] & 1 << (ch & 7)) != 0; }
    @Override public int[] getEscapeCodesForAscii() { return ascii; }
    @Override public SerializableString getEscapeSequence(int ch) {
        if (!isBlocked(ch)) return null;
        var result = new StringBuilder();
        for (char unit : Character.toChars(ch)) result.append(String.format(Locale.ROOT, "\\u%04X", (int) unit));
        return new SerializedString(result.toString());
    }
}
