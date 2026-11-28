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
@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound, packetIds=0x77, versions=EnumProtocolVersion.V12111, skip=true)
public class SSystemChatMessage implements IPacket
{
	public TextComponent Message = new TextComponent();

	public boolean Overlay;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		Message = TextComponentSerializer.deserialize(buffer);
		Overlay = buffer.readBoolean();
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		ByteBuf encoded = TextComponentSerializer.serialize(Message);
        try {buffer.writeBytes(encoded);} finally {encoded.release();}
		buffer.writeBoolean(Overlay);
	}

	public boolean handlePacket(GameConnection connection)
	{
		return false;
	}
}
