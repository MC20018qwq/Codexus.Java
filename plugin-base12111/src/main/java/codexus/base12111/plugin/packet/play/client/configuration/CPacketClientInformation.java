package codexus.base12111.plugin.packet.play.client.configuration;

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
@RegisterPacket(state=EnumConnectionState.Configuration, direction=EnumPacketDirection.ServerBound, packetIds=0x00, versions=EnumProtocolVersion.V12111)
public class CPacketClientInformation implements IPacket
{
	public String Locale = "";

	public int ViewDistance;

	public int ChatMode;

	public boolean ChatColors;

	public int DisplayedSkinParts;

	public int MainHand;

	public boolean EnableTextFiltering;

	public boolean AllowServerListings;

	public int ParticleStatus;

	public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}

	public void readFromBuffer(ByteBuf buffer)
	{
		Locale = NettyExtensions.readStringFromBuffer(buffer, 16);
		ViewDistance = buffer.readByte();
		ChatMode = NettyExtensions.readVarIntFromBuffer(buffer);
		ChatColors = buffer.readBoolean();
		DisplayedSkinParts = buffer.readUnsignedByte();
		MainHand = NettyExtensions.readVarIntFromBuffer(buffer);
		EnableTextFiltering = buffer.readBoolean();
		AllowServerListings = buffer.readBoolean();
		ParticleStatus = NettyExtensions.readVarIntFromBuffer(buffer);
	}

	public void writeToBuffer(ByteBuf buffer)
	{
		NettyExtensions.writeStringToBuffer(buffer, Locale, 16);
		buffer.writeByte((int)ViewDistance);
		NettyExtensions.writeVarInt(buffer, ChatMode);
		buffer.writeBoolean(ChatColors);
		buffer.writeByte((int)DisplayedSkinParts);
		NettyExtensions.writeVarInt(buffer, MainHand);
		buffer.writeBoolean(EnableTextFiltering);
		buffer.writeBoolean(AllowServerListings);
		NettyExtensions.writeVarInt(buffer, ParticleStatus);
	}

	public boolean handlePacket(GameConnection connection)
	{
        System.getLogger(getClass().getName()).log(System.Logger.Level.DEBUG, "Received client information: {0}", Locale);
		return false;
	}
}
