package codexus.heypixel.plugin.messages;

import io.netty.buffer.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.networking.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
import codexus.heypixel.plugin.crypto.PayloadScrambler;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public interface IAntiCheatMessage {
    void read(ByteBuf buffer,GameConnection connection);
    void write(ByteBuf buffer);
    default void write(ByteBuf buffer,GameConnection connection){write(buffer);}
    /** The original outbound messages have no inbound handling. */
    default void handle(GameConnection connection){}
    static byte[] encode(GameConnection connection,IAntiCheatMessage message,long timestamp){
        AntiCheatSession session=ConnectionStateAccessor.getAntiCheatSession(connection.ClientChannel,connection);ByteBuf body=Unpooled.buffer(),envelope=Unpooled.buffer();
        try{writeInt64(body,timestamp);message.write(body,connection);byte[] payload=readByteArrayReadableBytes(body);boolean protect=message instanceof AntiCheatReportMessage&&session.isChallengeAccepted();if(protect)payload=session.encrypt(payload);writeVarInt(envelope,AntiCheatMessageRegistry.getId(message.getClass()));writeVarInt(envelope,payload.length+1);if(protect)PayloadScrambler.write(envelope,payload,session.SessionId);else envelope.writeBytes(payload);return readByteArrayReadableBytes(envelope);}finally{body.release();envelope.release();}
    }
    static void send(GameConnection connection,IAntiCheatMessage message){
        try{HeypixelProtocolPlugin.sendPluginMessage(connection,HeypixelProtocolPlugin.AntiCheatChannel,
                encode(connection,message,System.currentTimeMillis()));}
        catch(RuntimeException e){System.getLogger(IAntiCheatMessage.class.getName()).log(System.Logger.Level.ERROR,"Error sending Heypixel message",e);}
    }
}
