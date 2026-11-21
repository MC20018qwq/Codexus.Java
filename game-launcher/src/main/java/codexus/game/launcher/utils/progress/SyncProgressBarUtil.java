package codexus.game.launcher.utils.progress;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SyncProgressBarUtil {
    private static final Object LOCK = new Object();
    private SyncProgressBarUtil() { }
    public enum ConsoleColor {
        Black(30), DarkRed(31), DarkGreen(32), DarkYellow(33), DarkBlue(34), DarkMagenta(35), DarkCyan(36), Gray(37),
        DarkGray(90), Red(91), Green(92), Yellow(93), Blue(94), Magenta(95), Cyan(96), White(97);
        private final int ansi;
        ConsoleColor(int ansi) { this.ansi = ansi; }
        String code() { return "\u001b[" + ansi + "m"; }
    }
    public static class ProgressBarOptions {
        public int Width = 45;
        public char FillChar = '■', EmptyChar = '·';
        public String ProgressFormat = "{0:P1}";
        public boolean ShowPercentage = true, ShowElapsedTime = true, ShowEta = true, ShowSpinner = true, LastLineNewline = true;
        public ConsoleColor FillColor = ConsoleColor.Cyan, EmptyColor = ConsoleColor.DarkGray, SpinnerColor = ConsoleColor.Cyan;
        public String Prefix = "", Suffix = "";
    }
    public static class ProgressReport {
        public int Percent;
        public String Message = "";
        public ProgressReport() { }
        public ProgressReport(int percent, String message) { Percent = percent; Message = message; }
    }
    public static final class ProgressBar implements AutoCloseable {
        private final int total;
        private final ProgressBarOptions options;
        private final long startTime = System.nanoTime();
        private final char[] spinner = {'|', '/', '─', '\\'};
        private int current, spinnerIndex;
        private boolean disposed;
        public ProgressBar(int total) { this(total, null); }
        public ProgressBar(int total, ProgressBarOptions options) {
            this.total = total; this.options = options == null ? new ProgressBarOptions() : options;
            if (this.options.Width < 0) throw new IllegalArgumentException("Progress width must be nonnegative");
        }
        public void update(int current, String action) {
            synchronized (LOCK) {
                if (disposed) return;
                this.current = current; spinnerIndex = (spinnerIndex + 1) % spinner.length;
                clearCurrent();
                double progress = total <= 0 ? 1 : Math.clamp((double) current / total, 0, 1);
                int filled = (int) (progress * options.Width);
                var output = new StringBuilder(options.Prefix);
                if (options.ShowSpinner) output.append((current < total ? options.SpinnerColor : ConsoleColor.Green).code())
                        .append(current < total ? spinner[spinnerIndex] : '✓').append(ConsoleColor.White.code()).append(' ');
                output.append('[').append(options.FillColor.code()).append(String.valueOf(options.FillChar).repeat(filled))
                        .append(options.EmptyColor.code()).append(String.valueOf(options.EmptyChar).repeat(options.Width - filled))
                        .append(ConsoleColor.White.code()).append("] ").append(action);
                if (options.ShowPercentage) output.append(' ').append(percentage(options.ProgressFormat, progress));
                long elapsed = (System.nanoTime() - startTime) / 1_000_000;
                if (options.ShowElapsedTime) output.append(" Elapsed: ").append(time(elapsed));
                if (options.ShowEta && current > 0) output.append(" ETA: ").append(time((long) ((double) elapsed / current * (total - current))));
                output.append(options.Suffix).append("\u001b[0m");
                System.out.print("\r" + output);
                if (current >= total && options.LastLineNewline) System.out.println();
            }
        }
        public static void clearCurrent() { synchronized (LOCK) { System.out.print("\r\u001b[2K\r"); } }
        @Override public void close() {
            synchronized (LOCK) { if (disposed) return; if (current < total) update(total, "Done"); disposed = true; }
        }
    }
    private static String time(long milliseconds) { long seconds = Math.max(0, milliseconds) / 1000; return String.format(Locale.ROOT, "%02d:%02d", seconds / 60 % 60, seconds % 60); }
    private static String percentage(String template, double progress) {
        var matcher = Pattern.compile("\\{0:[pP](\\d*)}").matcher(template);
        return matcher.replaceAll(match -> {
            int digits = match.group(1).isEmpty() ? 2 : Integer.parseInt(match.group(1));
            var format = NumberFormat.getPercentInstance(); format.setMinimumFractionDigits(digits); format.setMaximumFractionDigits(digits);
            return java.util.regex.Matcher.quoteReplacement(format.format(progress));
        }).replace("{0}", Double.toString(progress));
    }
}
