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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound, packetIds=0x30, versions=EnumProtocolVersion.V12111)
public class SPacketGameJoin implements IPacket
{
	private int PlayerId;

	private byte[] Payload = new byte[0];

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		PlayerId = buffer.readInt();
		Payload = NettyExtensions.readByteArrayReadableBytes(buffer);
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		buffer.writeInt(PlayerId);
		buffer.writeBytes(Payload);
	}

	public boolean handlePacket(GameConnection connection)
	{
		setLocalPlayer(connection, new LocalPlayer(PlayerId));
		setWorld(connection, new World());
		getTeams(connection).clear();
		return EventManager.Instance.triggerEvent(Base12111.PluginChannel, new EventGameJoin(connection)).IsCancelled;
	}
}
