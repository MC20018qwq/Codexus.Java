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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound, packetIds=0x1A, versions=EnumProtocolVersion.V1206)
public class CPacketSetPlayerPosition implements IPacket
{
	private double X;

	private double Y;

	private double Z;

	private boolean OnGround;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		X = buffer.readDouble();
		Y = buffer.readDouble();
		Z = buffer.readDouble();
		OnGround = buffer.readBoolean();
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		buffer.writeDouble(X);
		buffer.writeDouble(Y);
		buffer.writeDouble(Z);
		buffer.writeBoolean(OnGround);
	}

	public boolean handlePacket(GameConnection connection)
	{
		LocalPlayer localPlayer = getLocalPlayer(connection);
		if (localPlayer == null)
		{
			return false;
		}
		localPlayer.X = X;
		localPlayer.Y = Y;
		localPlayer.Z = Z;
		localPlayer.OnGround = OnGround;
		return false;
	}
}
