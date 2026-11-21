package codexus.cipher.game;

import codexus.development.sdk.game.GameCipherProvider;

/** Computes Java join packets locally, using launcher metadata from the NetEase patch list. */
public final class LocalGameCipherProvider implements GameCipherProvider {
    private X19GameCipherService cipher;
    private volatile boolean closed;

    public LocalGameCipherProvider() { }
    public LocalGameCipherProvider(X19GameCipherService cipher) {
        this.cipher = java.util.Objects.requireNonNull(cipher, "cipher");
    }
    private synchronized X19GameCipherService cipher() {
        if (closed) throw new IllegalStateException("Game cipher provider is closed");
        if (cipher == null) cipher = new X19GameCipherService();
        return cipher;
    }
    @Override public String authenticationBody(String serverId, long gameId, String version, String mods, String channel, int userId, String key) {
        return cipher().authenticationBody(serverId, gameId, version, mods, channel, userId, key);
    }
    @Override public String authenticationHandshake(int userId, String token, String context, String channel, String version) {
        return cipher().authenticationHandshake(userId, token, context, channel, version);
    }
    @Override public synchronized void close() { closed = true; }
}
