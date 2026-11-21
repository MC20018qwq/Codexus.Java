package codexus.gateway.manager;

import java.util.concurrent.*;

public final class TokenManager {
    public static final TokenManager Instance = new TokenManager();
    private final ConcurrentMap<String, String> tokens = new ConcurrentHashMap<>();
    public void updateToken(String id, String token) { tokens.put(id, token); }
    public void removeToken(String id) { tokens.remove(id); }
    public String getToken(String id) { return tokens.getOrDefault(id, ""); }
}
