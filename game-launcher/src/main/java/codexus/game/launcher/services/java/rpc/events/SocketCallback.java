package codexus.game.launcher.services.java.rpc.events;
import java.util.*;
import java.util.function.Consumer;
public class SocketCallback {
    private final Map<Integer, Consumer<byte[]>> callbacks = new HashMap<>();
    public Consumer<String> LostConnectCallback;
    public void registerReceiveCallback(int sid, Consumer<byte[]> callback) { callbacks.put(sid & 65535, callback); }
    public boolean invokeCallback(int sid, byte[] parameters) {
        if (!callbacks.containsKey(sid & 65535)) return false;
        callbacks.get(sid & 65535).accept(parameters); return true;
    }
}
