package codexus.base12111.plugin.event;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.manager.EventArgsBase;
import codexus.development.sdk.enums.EnumPacketDirection;
import codexus.base12111.plugin.packet.play.client.simulation.*;
import codexus.base12111.plugin.packet.play.server.patch.SSetEntityMetadata;
public class EventGameJoin extends EventArgsBase {

    public EventGameJoin(GameConnection connection) {
        super(connection);

    }
}
