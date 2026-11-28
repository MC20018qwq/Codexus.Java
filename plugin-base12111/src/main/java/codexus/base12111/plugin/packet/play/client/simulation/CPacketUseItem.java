package codexus.base12111.plugin.packet.play.client.simulation;

import codexus.base12111.plugin.Base12111;
import codexus.base12111.plugin.event.*;
import codexus.base12111.plugin.utils.*;
import codexus.base12111.plugin.utils.nbt.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.extensions.NettyExtensions;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.packet.*;
import codexus.development.sdk.utils.Position;
import io.netty.buffer.ByteBuf;
import static codexus.base12111.plugin.extensions.AttributeExtensions.*;
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound, packetIds=0x40, versions=EnumProtocolVersion.V12111)
public class CPacketUseItem implements IPacket
{
	public int Hand;

	public int Sequence;

	public float Yaw, Pitch;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		Hand = NettyExtensions.readVarIntFromBuffer(buffer);
		Sequence = NettyExtensions.readVarIntFromBuffer(buffer);
		Yaw = buffer.readFloat();
		Pitch = buffer.readFloat();
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeVarInt(buffer, Hand);
		NettyExtensions.writeVarInt(buffer, Sequence);
		buffer.writeFloat(Yaw);
		buffer.writeFloat(Pitch);
	}

	public boolean handlePacket(GameConnection connection)
	{
		return EventManager.Instance.triggerEvent(Base12111.PluginChannel, new EventUseItem(connection, this)).IsCancelled;
	}
}
