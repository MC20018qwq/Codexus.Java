package codexus.csharpsup;

import java.util.*;
import java.util.concurrent.CancellationException;

/** Cooperative cancellation for asynchronous I/O that must close outstanding resources. */
public final class CancellationToken {
    public static final CancellationToken NONE = new CancellationToken();
    private boolean cancelled;
    private final Set<Runnable> callbacks = new HashSet<>();
    CancellationToken() { }
    public synchronized boolean isCancellationRequested() { return cancelled; }
    public void throwIfCancellationRequested() {
        if (isCancellationRequested() || Thread.currentThread().isInterrupted()) throw new CancellationException();
    }
    public AutoCloseable register(Runnable callback) {
        synchronized (this) {
            if (!cancelled) { callbacks.add(callback); return () -> { synchronized (this) { callbacks.remove(callback); } }; }
        }
        callback.run();
        return () -> { };
    }
    void cancel() {
        List<Runnable> actions;
        synchronized (this) {
            if (cancelled) return;
            cancelled = true; actions = List.copyOf(callbacks); callbacks.clear();
        }
        for (Runnable callback : actions) { try { callback.run(); } catch (RuntimeException ignored) { } }
    }
}
