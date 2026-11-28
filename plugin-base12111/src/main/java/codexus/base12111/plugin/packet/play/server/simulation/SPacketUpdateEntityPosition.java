package codexus.base12111.plugin.packet.play.server.simulation;

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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound, packetIds=0x33, versions=EnumProtocolVersion.V12111)
public class SPacketUpdateEntityPosition implements IPacket
{
	private int EntityId;

	private short DeltaX;

	private short DeltaY;

	private short DeltaZ;

	private boolean OnGround;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		EntityId = NettyExtensions.readVarIntFromBuffer(buffer);
		DeltaX = buffer.readShort();
		DeltaY = buffer.readShort();
		DeltaZ = buffer.readShort();
		OnGround = buffer.readBoolean();
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeVarInt(buffer, EntityId);
		buffer.writeShort((int)DeltaX);
		buffer.writeShort((int)DeltaY);
		buffer.writeShort((int)DeltaZ);
		buffer.writeBoolean(OnGround);
	}

	public boolean handlePacket(GameConnection connection)
	{
		Entity entity = (getWorld(connection)==null?null:getWorld(connection).getEntity(EntityId));
		if (entity == null)
		{
			return false;
		}
		entity.applyRelativePosition(DeltaX, DeltaY, DeltaZ);
		entity.OnGround = OnGround;
		return false;
	}
}
