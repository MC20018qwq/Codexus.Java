package codexus.development.sdk.game;

/** Implemented by cipher so Java join computations stay local without a dependency cycle. */
public interface GameCipherProvider extends AutoCloseable {
    String authenticationBody(String serverId, long gameId, String gameVersion, String modInfo, String channel, int userId, String handshakeKey);
    String authenticationHandshake(int userId, String userToken, String context, String channel, String gameVersion);
    @Override void close();
}
