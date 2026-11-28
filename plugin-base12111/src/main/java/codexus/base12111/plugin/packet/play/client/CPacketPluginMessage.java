package codexus.base12111.plugin.packet.play.client;

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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ServerBound, packetIds=0x15, versions=EnumProtocolVersion.V12111)
public class CPacketPluginMessage implements IPacket
{
	public String Identifier = "";

	public byte[] Payload = new byte[0];

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		Identifier = NettyExtensions.readStringFromBuffer(buffer, 32767);
		Payload = NettyExtensions.readByteArrayReadableBytes(buffer);
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeStringToBuffer(buffer, Identifier, 32767);
		buffer.writeBytes(Payload);
	}

	public boolean handlePacket(GameConnection connection)
	{
		EventPluginMessage eventPluginMessage = EventManager.Instance.triggerEvent(Base12111.PluginChannel, new EventPluginMessage(connection, EnumPacketDirection.ServerBound, Identifier, Payload));
		Identifier = eventPluginMessage.Identifier;
		Payload = eventPluginMessage.Payload;
		return eventPluginMessage.IsCancelled;
	}
}
