package codexus.game.launcher.utils.progress;
import java.util.function.Consumer;
public class SyncCallback<T> implements Consumer<T> {
    private final Consumer<T> handler;
    public SyncCallback(Consumer<T> handler) { this.handler = handler; }
    public void report(T value) { handler.accept(value); }
    @Override public void accept(T value) { report(value); }
}
