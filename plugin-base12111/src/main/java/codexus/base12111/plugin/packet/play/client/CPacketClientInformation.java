package codexus.base12111.plugin.packet.play.client;

import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.RegisterPacket;

/** The common client-information codec has a distinct packet ID in each phase. */
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound,
        packetIds=0x0D, versions=EnumProtocolVersion.V12111)
public final class CPacketClientInformation extends
        codexus.base12111.plugin.packet.play.client.configuration.CPacketClientInformation {
}
