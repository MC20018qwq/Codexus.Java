package codexus.development.sdk.manager;
@FunctionalInterface public interface EventHandler<T extends IEventArgs> { void handle(T args); }
