package codexus.game.launcher.utils;
public final class HashUtil {
    private HashUtil() { }
    public static String generateGameRuntimeId(String gameId, String roleName) {
        return (gameId == null ? "" : gameId) + "-" + (roleName == null ? "" : roleName);
    }
}
