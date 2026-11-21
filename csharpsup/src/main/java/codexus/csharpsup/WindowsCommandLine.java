package codexus.csharpsup;

import java.util.*;

/** Windows backslash/quote rules for a ProcessStartInfo.Arguments value (no executable token). */
public final class WindowsCommandLine {
    private WindowsCommandLine() { }
    public static List<String> splitArguments(String arguments) {
        List<String> result = new ArrayList<>(); int index = 0;
        while (index < arguments.length()) {
            while (index < arguments.length() && (arguments.charAt(index) == ' ' || arguments.charAt(index) == '\t')) index++;
            if (index == arguments.length()) break;
            var token = new StringBuilder(); boolean quoted = false;
            while (index < arguments.length()) {
                char ch = arguments.charAt(index);
                if (!quoted && (ch == ' ' || ch == '\t')) break;
                int slashes = 0;
                while (index < arguments.length() && arguments.charAt(index) == '\\') { slashes++; index++; }
                if (index < arguments.length() && arguments.charAt(index) == '"') {
                    token.append("\\".repeat(slashes / 2));
                    if (slashes % 2 == 1) { token.append('"'); index++; }
                    else if (quoted && index + 1 < arguments.length() && arguments.charAt(index + 1) == '"') { token.append('"'); index += 2; }
                    else { quoted = !quoted; index++; }
                } else {
                    token.append("\\".repeat(slashes));
                    if (index < arguments.length()) {
                        ch = arguments.charAt(index);
                        if (!quoted && (ch == ' ' || ch == '\t')) break;
                        token.append(ch); index++;
                    }
                }
            }
            result.add(token.toString());
        }
        return result;
    }
}
