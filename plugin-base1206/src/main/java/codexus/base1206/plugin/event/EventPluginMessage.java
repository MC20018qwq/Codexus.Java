package codexus.base1206.plugin.event;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
import codexus.development.sdk.enums.EnumPacketDirection;
import codexus.base1206.plugin.packet.play.client.simulation.*;
import codexus.base1206.plugin.packet.play.server.patch.SSetEntityMetadata;
public class EventPluginMessage extends EventArgsBase {
    public EnumPacketDirection Direction;
    public String Identifier;
    public byte[] Payload;
    public EventPluginMessage(GameConnection connection, EnumPacketDirection direction, String identifier, byte[] payload) {
        super(connection);
        Direction = direction;
        Identifier = identifier;
        Payload = payload;
    }
}
