package codexus.development.sdk.event;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
public class EventEncryptionRequest extends EventArgsBase {public final String ServerId;public EventEncryptionRequest(GameConnection connection,String serverId){super(connection);ServerId=serverId;}}
