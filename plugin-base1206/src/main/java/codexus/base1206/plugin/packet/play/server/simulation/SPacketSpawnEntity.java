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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound, packetIds=0x01, versions=EnumProtocolVersion.V1206)
public class SPacketSpawnEntity implements IPacket
{
	private int EntityId;

	private byte[] EntityGuid = new byte[16];

	private int Type;

	private double X;

	private double Y;

	private double Z;

	private int Pitch;

	private int Yaw;

	private byte[] Data = new byte[0];

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		EntityId = NettyExtensions.readVarIntFromBuffer(buffer);
		buffer.readBytes(EntityGuid);
		Type = NettyExtensions.readVarIntFromBuffer(buffer);
		X = buffer.readDouble();
		Y = buffer.readDouble();
		Z = buffer.readDouble();
		Pitch = buffer.readUnsignedByte();
		Yaw = buffer.readUnsignedByte();
		Data = NettyExtensions.readByteArrayReadableBytes(buffer);
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeVarInt(buffer, EntityId);
		buffer.writeBytes(EntityGuid);
		NettyExtensions.writeVarInt(buffer, Type);
		buffer.writeDouble(X);
		buffer.writeDouble(Y);
		buffer.writeDouble(Z);
		buffer.writeByte((int)Pitch);
		buffer.writeByte((int)Yaw);
		buffer.writeBytes(Data);
	}

	public boolean handlePacket(GameConnection connection)
	{
		World world = getWorld(connection);
        if(world != null) {
            Entity entity = new Entity();
            entity.EntityId = EntityId;
            entity.EntityGuid = EntityGuid;
            entity.X = X;
            entity.Y = Y;
            entity.Z = Z;
            entity.Yaw = (float)(int)Yaw * 360f / 256f;
            entity.Pitch = (float)(int)Pitch * 360f / 256f;
            entity.OnGround = false;
            world.addEntity(entity);
        }
		return false;
	}
}
