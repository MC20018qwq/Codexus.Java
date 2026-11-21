package codexus.game.launcher.managers;
import java.util.concurrent.*;
public final class TaskManager {
    public static final TaskManager Instance = new TaskManager();
    private TaskManager() { }
    public Executor getFactory() { return ForkJoinPool.commonPool(); }
}
