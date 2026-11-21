package codexus.gateway.manager;

import codexus.development.sdk.utils.ClassCatalog;
import codexus.gateway.connection.ConnectionState;
import codexus.gateway.message.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public final class MessageManager {
    private final ConcurrentMap<ConnectionState, ConcurrentMap<String, Supplier<? extends IMessage>>> factories = new ConcurrentHashMap<>();
    private final ConcurrentMap<ConnectionState, ConcurrentMap<String, Class<? extends IMessage>>> types = new ConcurrentHashMap<>();
    public MessageManager() {
        try {
            for (Class<?> type : ClassCatalog.classes(Path.of(MessageManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()), MessageManager.class.getClassLoader())) {
                var registration = type.getDeclaredAnnotation(RegisterMessage.class);
                if (registration == null || !IMessage.class.isAssignableFrom(type)) continue;
                @SuppressWarnings("unchecked") var messageType = (Class<? extends IMessage>) type;
                registerMessage(registration.state(), registration.type(), messageType);
            }
        } catch (Exception e) { throw new IllegalStateException("Unable to register Gateway messages", e); }
    }
    public <T extends IMessage> void registerMessage(ConnectionState state, String name, Class<T> type) {
        try {
            var constructor = type.getConstructor();
            factories.computeIfAbsent(state, ignored -> new ConcurrentHashMap<>()).putIfAbsent(name, () -> {
                try { return constructor.newInstance(); } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
            });
            types.computeIfAbsent(state, ignored -> new ConcurrentHashMap<>()).putIfAbsent(name, type);
        } catch (NoSuchMethodException e) { throw new IllegalArgumentException("Message requires a public no-argument constructor", e); }
    }
    public IMessage buildMessage(ConnectionState state, String name) {
        var supplier = factories.getOrDefault(state, new ConcurrentHashMap<>()).get(name);
        return supplier == null ? null : supplier.get();
    }
    public Map<String, Class<? extends IMessage>> getRegisteredMessages(ConnectionState state) { return Map.copyOf(types.getOrDefault(state, new ConcurrentHashMap<>())); }
}
