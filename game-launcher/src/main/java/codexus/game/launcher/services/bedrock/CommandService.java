package codexus.game.launcher.services.bedrock;

import java.io.*;
public final class CommandService {
    private CommandService() { }
    public static Process startGame(String exePath) {
        try { return new ProcessBuilder(exePath).inheritIO().start(); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
