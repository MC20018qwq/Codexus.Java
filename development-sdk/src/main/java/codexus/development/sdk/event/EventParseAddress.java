package codexus.development.sdk.event;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
public class EventParseAddress extends EventArgsBase {public String Address; public int Port; public EventParseAddress(GameConnection connection,String address,int port){super(connection);Address=address;Port=port;}}
