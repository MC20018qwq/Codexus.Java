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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound, packetIds=0x38, versions=EnumProtocolVersion.V1206)
public class CPacketUseItemOn implements IPacket
{
	public int Hand;

	public Position Location = new Position(0, 0, 0);

	public int Face;

	public float CursorPositionX;

	public float CursorPositionY;

	public float CursorPositionZ;

	public boolean InsideBlock;

	public int Sequence;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		Hand = NettyExtensions.readVarIntFromBuffer(buffer);
		Location = NettyExtensions.readPosition(buffer);
		Face = NettyExtensions.readVarIntFromBuffer(buffer);
		CursorPositionX = buffer.readFloat();
		CursorPositionY = buffer.readFloat();
		CursorPositionZ = buffer.readFloat();
		InsideBlock = buffer.readBoolean();
		Sequence = NettyExtensions.readVarIntFromBuffer(buffer);
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeVarInt(buffer, Hand);
		NettyExtensions.writePosition(buffer, Location);
		NettyExtensions.writeVarInt(buffer, Face);
		buffer.writeFloat(CursorPositionX);
		buffer.writeFloat(CursorPositionY);
		buffer.writeFloat(CursorPositionZ);
		buffer.writeBoolean(InsideBlock);
		NettyExtensions.writeVarInt(buffer, Sequence);
	}

	public boolean handlePacket(GameConnection connection)
	{
		return EventManager.Instance.triggerEvent(Base1206.PluginChannel, new EventUseItemOn(connection, this)).IsCancelled;
	}
}
