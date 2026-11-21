package codexus.development.sdk.raknet;
import java.util.UUID;
public interface IRakNet {
    UUID getId();
    String getRemoteAddress();
    String getLocalAddress();
    String getRoleName();
    String getGameName();
    void shutdown();
}
