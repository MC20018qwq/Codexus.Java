package codexus.base1206.plugin.event;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
import codexus.development.sdk.enums.EnumPacketDirection;
import codexus.base1206.plugin.packet.play.client.simulation.*;
import codexus.base1206.plugin.packet.play.server.patch.SSetEntityMetadata;
public class EventSwingArm extends EventArgsBase {
    public int Hand;
    public EventSwingArm(GameConnection connection, int hand) {
        super(connection);
        Hand = hand;
    }
}
