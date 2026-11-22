package codexus.heypixel.plugin.messages;

import io.netty.buffer.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.networking.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.readByteArrayReadableBytes;
public final class AntiCheatChallengeMessage implements IAntiCheatMessage {
    public String Key="",Payload="";
    public AntiCheatReportType ReportType=AntiCheatReportType.Info;
    public long Timestamp;
    public void read(ByteBuf b,GameConnection c){AntiCheatSession session=ConnectionStateAccessor.getAntiCheatSession(c.ClientChannel,c);ByteBuf payload=Unpooled.wrappedBuffer(session.decrypt(readByteArrayReadableBytes(b)));try{Key=readString(payload);Timestamp=readInt64(payload);ReportType=AntiCheatReportType.fromValue(readInt32(payload));Payload=ReportType==AntiCheatReportType.ReflectCheck?readString(payload):"";session.acceptChallenge();}finally{payload.release();}}
    public void write(ByteBuf b){throw new IllegalStateException("Server message, cannot send.");}
    public void handle(GameConnection c){HeypixelProtocolPlugin.sendNotice(c,"[Heypixel] 正在偷取你的客户端数据 Σ( ° △ °|||)︴, 类型: "+ReportType,"yellow");ConnectionStateAccessor.getAntiCheatSession(c.ClientChannel,c).report(ReportType,Key,Timestamp,Payload);}
}
