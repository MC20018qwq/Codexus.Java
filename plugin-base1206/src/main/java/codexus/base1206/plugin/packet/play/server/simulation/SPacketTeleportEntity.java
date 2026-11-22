package codexus.base1206.plugin.packet.play.server.simulation;

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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound, packetIds=0x70, versions=EnumProtocolVersion.V1206)
public class SPacketTeleportEntity implements IPacket
{
	private int EntityId;

	private double X;

	private double Y;

	private double Z;

	private int Yaw;

	private int Pitch;

	private boolean OnGround;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		EntityId = NettyExtensions.readVarIntFromBuffer(buffer);
		X = buffer.readDouble();
		Y = buffer.readDouble();
		Z = buffer.readDouble();
		Yaw = buffer.readUnsignedByte();
		Pitch = buffer.readUnsignedByte();
		OnGround = buffer.readBoolean();
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeVarInt(buffer, EntityId);
		buffer.writeDouble(X);
		buffer.writeDouble(Y);
		buffer.writeDouble(Z);
		buffer.writeByte((int)Yaw);
		buffer.writeByte((int)Pitch);
		buffer.writeBoolean(OnGround);
	}

	public boolean handlePacket(GameConnection connection)
	{
		Entity entity = (getWorld(connection)==null?null:getWorld(connection).getEntity(EntityId));
		if (entity == null)
		{
			return false;
		}
		entity.X = X;
		entity.Y = Y;
		entity.Z = Z;
		entity.Yaw = (float)(int)Yaw * 360f / 256f;
		entity.Pitch = (float)(int)Pitch * 360f / 256f;
		entity.OnGround = OnGround;
		return false;
	}
}
