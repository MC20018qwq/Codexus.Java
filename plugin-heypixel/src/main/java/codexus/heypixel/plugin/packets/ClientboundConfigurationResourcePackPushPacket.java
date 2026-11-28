package codexus.heypixel.plugin.packets;

import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.RegisterPacket;

@RegisterPacket(state=EnumConnectionState.Configuration, direction=EnumPacketDirection.ClientBound,
        packetIds=9, versions=EnumProtocolVersion.V12111, skip=true)
public final class ClientboundConfigurationResourcePackPushPacket extends ClientboundResourcePackPushPacket {
}
