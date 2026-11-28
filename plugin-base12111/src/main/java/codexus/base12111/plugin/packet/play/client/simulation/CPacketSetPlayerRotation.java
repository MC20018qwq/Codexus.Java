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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound, packetIds=0x1F, versions=EnumProtocolVersion.V12111)
public class CPacketSetPlayerRotation implements IPacket
{
	private float Yaw;

	private float Pitch;

	private boolean OnGround;

	private boolean HorizontalCollision;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		Yaw = buffer.readFloat();
		Pitch = buffer.readFloat();
		int flags = buffer.readUnsignedByte();
		OnGround = (flags & 1) != 0;
		HorizontalCollision = (flags & 2) != 0;
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		buffer.writeFloat(Yaw);
		buffer.writeFloat(Pitch);
		buffer.writeByte((OnGround ? 1 : 0) | (HorizontalCollision ? 2 : 0));
	}

	public boolean handlePacket(GameConnection connection)
	{
		LocalPlayer localPlayer = getLocalPlayer(connection);
		if (localPlayer == null)
		{
			return false;
		}
		localPlayer.Yaw = Yaw;
		localPlayer.Pitch = Pitch;
		localPlayer.OnGround = OnGround;
		localPlayer.HorizontalCollision = HorizontalCollision;
		return false;
	}
}
