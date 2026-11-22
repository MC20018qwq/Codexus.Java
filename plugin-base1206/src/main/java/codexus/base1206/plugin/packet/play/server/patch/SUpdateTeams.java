package codexus.base1206.plugin.packet.play.server.patch;

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
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import codexus.base1206.plugin.utils.patch.Teams;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@RegisterPacket(state=EnumConnectionState.Play,direction=EnumPacketDirection.ClientBound,packetIds=0x60,versions=EnumProtocolVersion.V1206)
public class SUpdateTeams implements IPacket {
    private String TeamName="";
    private int Method;
    private byte[] RemainingBytes;
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}
    public void readFromBuffer(ByteBuf buffer){TeamName=readStringFromBuffer(buffer,32767);Method=buffer.readUnsignedByte();RemainingBytes=readByteArrayReadableBytes(buffer);}
    public void writeToBuffer(ByteBuf buffer){writeStringToBuffer(buffer,TeamName,32767);buffer.writeByte(Method);if(RemainingBytes!=null)buffer.writeBytes(RemainingBytes);}
    public boolean handlePacket(GameConnection connection){Teams teams=getTeams(connection);return switch(Method){case 0->handleCreateTeam(teams);case 1->handleRemoveTeam(teams);case 3->updateTeamEntities(teams,true);case 4->updateTeamEntities(teams,false);default->false;};}
    private boolean handleCreateTeam(Teams teams){if(teams.containsTeam(TeamName))return true;teams.createTeam(TeamName);return false;}
    private boolean handleRemoveTeam(Teams teams){if(!teams.containsTeam(TeamName))return true;teams.removeTeam(TeamName);return false;}
    private boolean updateTeamEntities(Teams teams,boolean add){
        if(!teams.containsTeam(TeamName)||RemainingBytes==null)return true;
        var forwarded=new ArrayList<String>(); ByteBuf input=Unpooled.wrappedBuffer(RemainingBytes);
        try {int count=readVarIntFromBuffer(input); if(count<0)throw new IllegalArgumentException("Negative team member count");for(int i=0;i<count;i++){String entity=readStringFromBuffer(input,32767);if(add){String old=teams.findPlayerTeam(entity);if(old!=null&&!old.equals(TeamName))teams.removePlayerFromTeam(old,entity);if(teams.addPlayerToTeam(TeamName,entity))forwarded.add(entity);}else if(teams.removePlayerFromTeam(TeamName,entity))forwarded.add(entity);}}finally{input.release();}
        ByteBuf output=Unpooled.buffer();try{writeVarInt(output,forwarded.size());for(String entity:forwarded)writeStringToBuffer(output,entity,32767);RemainingBytes=readByteArrayReadableBytes(output);}finally{output.release();}return false;
    }
}
