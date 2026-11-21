package codexus.csharpsup;

public final class CancellationTokenSource implements AutoCloseable {
    private final CancellationToken token = new CancellationToken();
    public CancellationToken getToken() { return token; }
    public void cancel() { token.cancel(); }
    @Override public void close() { cancel(); }
}
