package codexus.heypixel.plugin.messages;

import io.netty.buffer.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.networking.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
import codexus.heypixel.plugin.metadata.*;
import java.util.Base64;
import java.util.Arrays;
public final class AntiCheatReportMessage implements IAntiCheatMessage {
    public AntiCheatSession Session;
    public Uuid128 SessionId;
    public AntiCheatReportType ReportType;
    public String Key,Payload;
    public long Timestamp;
    public void read(ByteBuf b,GameConnection c){throw new IllegalStateException("Client message, cannot receive.");}
    /** Report serialization requires the connection overload, as in the original. */
    public void write(ByteBuf b){}
    public void write(ByteBuf b,GameConnection c){writeString(b,SessionId.toString());writeByte(b,ReportType.value());writeString(b,Key);writeInt64(b,Timestamp);switch(ReportType){case Info->Session.Environment.writeInfoReport(b);case BlackClass->{writeInt32(b,Session.BlacklistedClassCount);writeInt32(b,1);writeArrayHeader(b,1);writeString(b,Session.BlacklistedClassName);}case BlackModule->{writeInt32(b,LoadedModuleFingerprints.count());writeInt32(b,1);writeArrayHeader(b,1);writeString(b,Session.BlacklistedModuleName+":"+Session.BlacklistedModuleVersion);}case ReflectCheck->writeReflectionCheck(b,c);}}
    private void writeReflectionCheck(ByteBuf b,GameConnection c){ReflectionCheckReport report=ReflectionCheckResultParser.parse(Payload);HeypixelProtocolPlugin.sendNotice(c,"[Heypixel] 检测到反射调用链, 已阻止其非法获取数据，并修补安全漏洞, 最终类型: "+report.FinalAction,"yellow");if(!"getStaticField".equals(report.FinalAction)||report.AccessPath==null)return;String[] parts=report.AccessPath.split("\\.",-1);if(parts.length<3)throw new IllegalArgumentException("Incomplete reflection access path");String field=ReflectionMetadataRepository.normalizeName(parts[parts.length-1]),enumName=ReflectionMetadataRepository.normalizeName(parts[parts.length-2]),classPath=String.join(".",Arrays.copyOf(parts,parts.length-2));EnumFieldMetadata metadata=ReflectionMetadataRepository.tryGetField(ReflectionMetadataRepository.normalizeName(classPath),enumName,field);if(metadata==null){String error="CheckError:"+classPath;writeInt32(b,error.hashCode());writeString(b,Base64.getEncoder().encodeToString(Session.encrypt(error)));return;}writeInt32(b,metadata.HashCode);writeString(b,Base64.getEncoder().encodeToString(Session.encrypt(metadata.Content)));}
}
