package codexus.base1206.plugin.packet.play.client.simulation;

import codexus.base1206.plugin.Base1206;
import codexus.base1206.plugin.event.*;
import codexus.base1206.plugin.utils.*;
import codexus.base1206.plugin.utils.nbt.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.extensions.NettyExtensions;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.packet.*;
import codexus.development.sdk.utils.Position;
import io.netty.buffer.ByteBuf;
import static codexus.base1206.plugin.extensions.AttributeExtensions.*;
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound, packetIds=0x16, versions=EnumProtocolVersion.V1206, skip=true)
public class CPacketInteract implements IPacket
{
	public int EntityId;

	public int Type;

	public float TargetX;

	public float TargetY;

	public float TargetZ;

	public int Hand;

	public boolean Sneaking;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		EntityId = NettyExtensions.readVarIntFromBuffer(buffer);
		Type = NettyExtensions.readVarIntFromBuffer(buffer);
		if (Type == 2)
		{
			TargetX = buffer.readFloat();
			TargetY = buffer.readFloat();
			TargetZ = buffer.readFloat();
		}
		int type = Type;
		if ((type == 0 || type == 2) ? true : false)
		{
			Hand = NettyExtensions.readVarIntFromBuffer(buffer);
		}
		Sneaking = buffer.readBoolean();
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeVarInt(buffer, EntityId);
		NettyExtensions.writeVarInt(buffer, Type);
		if (Type == 2)
		{
			buffer.writeFloat(TargetX);
			buffer.writeFloat(TargetY);
			buffer.writeFloat(TargetZ);
		}
		int type = Type;
		if (type == 0 || type == 2)
		{
			NettyExtensions.writeVarInt(buffer, Hand);
		}
		buffer.writeBoolean(Sneaking);
	}

	public boolean handlePacket(GameConnection connection)
	{
		return EventManager.Instance.triggerEvent(Base1206.PluginChannel, new EventInteract(connection, this)).IsCancelled;
	}
}
