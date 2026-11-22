package codexus.base1206.plugin.event;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
import codexus.development.sdk.enums.EnumPacketDirection;
import codexus.base1206.plugin.packet.play.client.simulation.*;
import codexus.base1206.plugin.packet.play.server.patch.SSetEntityMetadata;
public class EventSetEntityMetadata extends EventArgsBase {
    public SSetEntityMetadata Packet;
    public EventSetEntityMetadata(GameConnection connection, SSetEntityMetadata packet) {
        super(connection);
        Packet = packet;
    }
}
