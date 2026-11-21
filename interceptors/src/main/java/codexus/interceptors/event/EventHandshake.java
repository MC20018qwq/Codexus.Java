package codexus.interceptors.event;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
import codexus.interceptors.packet.handshake.client.CHandshake;

public class EventHandshake extends EventArgsBase {
    public final CHandshake Handshake;
    public EventHandshake(GameConnection connection, CHandshake handshake) {
        super(connection);
        Handshake = handshake;
    }
}
