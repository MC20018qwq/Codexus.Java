package codexus.heypixel.plugin.anticheat;

import codexus.csharpsup.DotNetEncoding;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.EnumConnectionState;
import codexus.base12111.plugin.packet.play.client.CPacketPluginMessage;
import codexus.base12111.plugin.utils.nbt.TextComponent;
import codexus.heypixel.plugin.crypto.*;
import codexus.heypixel.plugin.messages.*;
import codexus.heypixel.plugin.models.Uuid128;
import codexus.heypixel.plugin.packets.ClientboundPlayDisconnectPacket;
import codexus.heypixel.plugin.utilities.LauncherGamePaths;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
public final class AntiCheatSession {
    private final GameConnection connection;
    private SaltedDesCipher cipher;
    private final AtomicBoolean challengeAccepted=new AtomicBoolean(),initialReportSent=new AtomicBoolean();
    private long nextSentinelRefreshTimestamp=-1;
    public static volatile boolean IsLocalHookConfigured;
    public final int BlacklistedClassCount=ThreadLocalRandom.current().nextInt(77772,81079);
    public final CpsTracker Cps=new CpsTracker();
    public final ClientFingerprintBuilder Environment;
    public Uuid128 SessionId;
    public String BlacklistedModuleName="",BlacklistedClassName="",BlacklistedModuleVersion="";
    public volatile boolean IsHandshakeComplete,HasKeyDerivationFailed;
    public AntiCheatSession(GameConnection connection){this(connection,null,LauncherGamePaths.CacheRoot);}
    public AntiCheatSession(GameConnection connection,Uuid128 selectedId,Path cacheRoot){
        this.connection=connection;SessionId=Uuid128.newRandom();
        try{if(selectedId!=null)SessionId=selectedId;else{var value=new HeypixelKeyDerivationService().derive(new Uuid128(connection.Uuid).toUuid(),Long.parseLong(connection.Session.UserId));SessionId=new Uuid128(value.getMostSignificantBits(),value.getLeastSignificantBits());}}catch(RuntimeException e){HasKeyDerivationFailed=true;System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"Local Heypixel key derivation failed",e);disconnectForKeyDerivationFailure();}
        if(IsLocalHookConfigured)SessionId=Uuid128.newRandom();cipher=new SaltedDesCipher(SessionId.toString());Environment=new ClientFingerprintBuilder(connection.Session,cipher,connection.GameId,cacheRoot);refreshEncryptedSentinelValues();
    }
    public boolean hasSentInitialReport(){return initialReportSent.get();}
    public boolean isChallengeAccepted(){return challengeAccepted.get();}
    public boolean isReportEncryptionEnabled(){return isChallengeAccepted();}
    public boolean isPayloadScramblingEnabled(){return isChallengeAccepted();}
    public synchronized void beginHandshake(){if(HasKeyDerivationFailed)return;IsHandshakeComplete=true;long now=System.currentTimeMillis();if(nextSentinelRefreshTimestamp<=0||now>nextSentinelRefreshTimestamp){nextSentinelRefreshTimestamp=now+30000;refreshEncryptedSentinelValues();}}
    public void sendInitialReport(){if(HasKeyDerivationFailed||!initialReportSent.compareAndSet(false,true))return;report(AntiCheatReportType.Info,SessionId.toEncodedString(),System.currentTimeMillis());}
    public void acceptChallenge(){challengeAccepted.set(true);}
    public void syncSkins(){if(HasKeyDerivationFailed)return;CPacketPluginMessage packet=new CPacketPluginMessage();packet.Identifier="heypixel:sync_skins";packet.Payload=DotNetEncoding.utf8("ASQwMDAwMDAwMC0wMDAwLTQwMDAtODAwMC0wMDAwMzliYzYyMTM=");if(connection.ServerChannel!=null)connection.ServerChannel.writeAndFlush(packet);}
    public void report(AntiCheatReportType type,String key,long timestamp){report(type,key,timestamp,"");}
    public void report(AntiCheatReportType type,String key,long timestamp,String payload){if(HasKeyDerivationFailed)return;AntiCheatReportMessage m=new AntiCheatReportMessage();m.Session=this;m.SessionId=SessionId;m.ReportType=type;m.Key=key;m.Timestamp=timestamp;m.Payload=payload;IAntiCheatMessage.send(connection,m);}
    public byte[] encrypt(String value){return encrypt(DotNetEncoding.utf8(value));}
    public byte[] encrypt(byte[] value){return cipher.encrypt(value);}
    public byte[] decrypt(byte[] value){return cipher.decrypt(value);}
    public static int computeFnv1a(int value){int hash=(int)2166136261L;for(int i=0;i<4;i++){hash^=(value>>>(i*8))&255;hash*=16777619;}return hash;}
    private void refreshEncryptedSentinelValues(){BlacklistedModuleName=cipher.encryptString(SessionId.toString());BlacklistedModuleVersion=cipher.encryptString("0");BlacklistedClassName=cipher.encryptString("0");}
    private void disconnectForKeyDerivationFailure(){
        Runnable check=new Runnable(){public void run(){if(connection.TaskGroup.isShuttingDown()||!connection.ClientChannel.isActive())return;if(connection.State!=EnumConnectionState.Play){connection.TaskGroup.schedule(this,100,TimeUnit.MILLISECONDS);return;}TextComponent reason=new TextComponent();reason.Text="[Codexus]\n\n为了防止您被封禁，我们已将你踢出服务器\n\n原因：密钥派生异常\n建议：请检查网络连接或稍后重试";reason.Color="yellow";ClientboundPlayDisconnectPacket packet=new ClientboundPlayDisconnectPacket();packet.Reason=reason;connection.ClientChannel.writeAndFlush(packet).addListener(f->connection.shutdown());}};connection.TaskGroup.schedule(check,100,TimeUnit.MILLISECONDS);
    }
}
