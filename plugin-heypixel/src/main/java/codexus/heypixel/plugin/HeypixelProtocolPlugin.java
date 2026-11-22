package codexus.heypixel.plugin;

import codexus.development.sdk.attributes.Plugin;
import codexus.development.sdk.plugin.IPlugin;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.event.EventLoginSuccess;
import codexus.development.sdk.manager.*;
import codexus.interceptors.event.EventHandshake;
import codexus.interceptors.packet.login.server.SPacketDisconnect;
import codexus.base1206.plugin.event.*;
import codexus.base1206.plugin.packet.play.client.CPacketPluginMessage;
import codexus.base1206.plugin.utils.*;
import codexus.base1206.plugin.utils.nbt.TextComponent;
import codexus.base1206.plugin.utils.patch.metadata.EntityDataSerializers;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.networking.*;
import codexus.heypixel.plugin.metadata.ReflectionMetadataRepository;
import codexus.heypixel.plugin.messages.*;
import codexus.csharpsup.DotNetEncoding;
import io.netty.buffer.*;
import io.netty.util.AttributeKey;
import java.util.*;
import java.util.concurrent.*;
import static codexus.base1206.plugin.extensions.AttributeExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@Plugin(id="F110DA9F-F0CB-F926-C72C-FEAC7FCF3601",name="Heypixel Protocol",description="A lightweight protocol plugin for Heypixel, enabling seamless compatibility and optimized performance.",author="DevCodexus",version="2.3.3",dependencies={"FEF71E62-1DA0-4935-AE43-49102690B32F"})
public final class HeypixelProtocolPlugin implements IPlugin,AutoCloseable {
    public static final String TargetGameId="4661334467366178884",AntiCheatChannel="heypixel:s2cevent";
    private static final long PHASE_ORIGIN=monotonicMillis();
    private static final AttributeKey<Boolean> TIMERS=AttributeKey.valueOf("heypixel:timers-started");
    private static final System.Logger LOG=System.getLogger(HeypixelProtocolPlugin.class.getName());
    private static volatile HeypixelProtocolPlugin instance;
    private final List<String> requiredPluginChannels=List.of("worldedit:cui","fml:loginwrapper","forge:tier_sorting","storemod:buy","floodgate:custom","floodgate:packet",AntiCheatChannel,"report:areport","plugin:guild","fml:play","floodgate:netease","floodgate:transfer","fml:handshake","heypixel:onlinestats","forge:split","floodgate:form","geckolib:main","floodgate:skin");
    private final LocalHttpListenerServer hookServer=new LocalHttpListenerServer();
    private final List<Runnable> unregister=new ArrayList<>();
    private final Set<io.netty.util.concurrent.ScheduledFuture<?>> timers=ConcurrentHashMap.newKeySet();
    private boolean initialized;
    public HeypixelProtocolPlugin(){instance=this;}
    public static HeypixelProtocolPlugin getInstance(){var current=instance;if(current==null)throw new IllegalStateException("The Heypixel protocol plugin is not initialized.");return current;}
    @Override public synchronized void onInitialize(){ initialize(true); }
    public synchronized void initializeEmbedded(){ initialize(false); }
    private void initialize(boolean startHookListener){
        if(initialized)return;AntiCheatMessageRegistry.registerDefaults();ReflectionMetadataRepository.initialize();
        register(EventLoginSuccess.class,"channel_v1206",this::onLoginSuccess);register(EventPluginMessage.class,"base_1206",this::onPluginMessage);register(EventSwingArm.class,"base_1206",HeypixelProtocolPlugin::onSwingArm);register(EventGameJoin.class,"base_1206",HeypixelProtocolPlugin::onGameJoin);register(EventUseItemOn.class,"base_1206",HeypixelProtocolPlugin::onUseItemOn);register(EventUseItem.class,"base_1206",HeypixelProtocolPlugin::onUseItem);register(EventSetEntityMetadata.class,"base_1206",HeypixelProtocolPlugin::onEntityMetadata);register(EventHandshake.class,"channel_v1206",HeypixelProtocolPlugin::onHandshake);
        if(startHookListener) hookServer.startAsync().whenComplete((v,error)->{if(error!=null)LOG.log(System.Logger.Level.WARNING,"Cannot start local Heypixel hook listener",error);});initialized=true;
    }
    private <T extends IEventArgs> void register(Class<T> type,String channel,EventHandler<T> handler){EventManager.Instance.registerHandler(type,channel,handler);unregister.add(()->EventManager.Instance.unregisterHandler(type,channel,handler));}
    /** The source defines this notification extension point without a body. */
    public static void sendNotice(GameConnection connection,String message,String color){}
    private static void onEntityMetadata(EventSetEntityMetadata e){if(!isTargetGame(e.Connection))return;LocalPlayer player=getLocalPlayer(e.Connection);if(player==null)return;if(e.Packet.EntityId==player.PlayerId){int pose=EntityDataSerializers.getSerializedId(EntityDataSerializers.Pose);e.Packet.PackedItems.removeIf(item->item.serializerId()==pose);}}
    private static void onHandshake(EventHandshake e){if(!isTargetGame(e.Connection)||e.Handshake.ProtocolVersion==766)return;GameConnection c=e.Connection;LOG.log(System.Logger.Level.WARNING,"布吉岛要求使用 Minecraft 1.20.1 -> (ViaForge) -> Minecraft 1.20.6 版本进入服务器。");if(c.ProtocolVersion==EnumProtocolVersion.V1206)return;TextComponent reason=new TextComponent();reason.Text="[Codexus] 布吉岛要求使用 Minecraft 1.20.1 -> (ViaForge) -> Minecraft 1.20.6 版本进入服务器，否则将会被检测并封禁。";reason.Color="yellow";SPacketDisconnect packet=new SPacketDisconnect();packet.Reason=reason.toJson();c.ClientChannel.writeAndFlush(packet).addListener(f->c.shutdown());}
    private static void onGameJoin(EventGameJoin e){if(!isTargetGame(e.Connection))return;AntiCheatSession session=session(e.Connection);session.beginHandshake();session.sendInitialReport();}
    private static void onUseItem(EventUseItem e){if(isTargetGame(e.Connection))session(e.Connection).Cps.recordRightClick();}
    private static void onSwingArm(EventSwingArm e){if(isTargetGame(e.Connection))session(e.Connection).Cps.recordLeftClick();}
    private static void onUseItemOn(EventUseItemOn e){if(!isTargetGame(e.Connection))return;LocalPlayer p=getLocalPlayer(e.Connection);if(p==null)return;UseItemOnMessage m=new UseItemOnMessage();m.PlayerX=(float)p.X;m.PlayerY=(float)p.Y;m.PlayerZ=(float)p.Z;m.Direction=e.Data.Face;m.InteractionType=1;m.HitX=(float)e.Data.Location.X+e.Data.CursorPositionX;m.HitY=(float)e.Data.Location.Y+e.Data.CursorPositionY;m.HitZ=(float)e.Data.Location.Z+e.Data.CursorPositionZ;m.BlockX=e.Data.Location.X;m.BlockY=e.Data.Location.Y;m.BlockZ=e.Data.Location.Z;m.InsideBlock=e.Data.InsideBlock;m.Yaw=p.Yaw;m.Pitch=p.Pitch;m.IsHand=e.Data.Hand==0;IAntiCheatMessage.send(e.Connection,m);}
    private void onPluginMessage(EventPluginMessage e){if(!isTargetGame(e.Connection))return;switch(e.Direction){case ServerBound->handleServerboundPluginMessage(e);case ClientBound->handleClientboundPluginMessage(e);default->throw new IllegalArgumentException("Invalid packet direction");}}
    private void onLoginSuccess(EventLoginSuccess e){GameConnection c=e.Connection;if(!isTargetGame(c)||c.ClientChannel.attr(TIMERS).setIfAbsent(true)!=null)return;AntiCheatSession session=session(c);CpsUpdateMessage update=new CpsUpdateMessage();long[] last={-1};
        track(c.TaskGroup.scheduleAtFixedRate(()->{if(c.State!=EnumConnectionState.Play||!session.IsHandshakeComplete)return;try{int left=session.Cps.getLeftCps(),right=session.Cps.getRightCps();if(update.LeftCps==left&&update.RightCps==right)return;long now=monotonicMillis();if(!isCpsUpdateDue(last[0],now))return;update.LeftCps=left;update.RightCps=right;IAntiCheatMessage.send(c,update);last[0]=now;}catch(RuntimeException ex){LOG.log(System.Logger.Level.ERROR,"Error sending CPS message",ex);}},50,50,TimeUnit.MILLISECONDS));
        int delay=getKeepAliveDelayMilliseconds(Math.max(0,monotonicMillis()-PHASE_ORIGIN));KeepAliveMessage keepAlive=new KeepAliveMessage();track(c.TaskGroup.scheduleAtFixedRate(()->{if(c.State==EnumConnectionState.Play&&session.IsHandshakeComplete)IAntiCheatMessage.send(c,keepAlive);},delay,5000,TimeUnit.MILLISECONDS));
    }
    private void track(io.netty.util.concurrent.ScheduledFuture<?> future){timers.add(future);future.addListener(done->timers.remove(future));}
    public static boolean isCpsUpdateDue(long last,long current){return last<0||current-last>=250;}
    public static int getKeepAliveDelayMilliseconds(long elapsed){if(elapsed<=0)return 5000;long remainder=elapsed%5000;return remainder==0?0:5000-(int)remainder;}
    private void handleClientboundPluginMessage(EventPluginMessage e){switch(e.Identifier){case "minecraft:register"->handleChannelRegistration(e);case AntiCheatChannel->handleAntiCheatMessage(e);case "floodgate:form"->handleFloodgateForm(e);default->{}}}
    private void handleChannelRegistration(EventPluginMessage e){PluginChannelRegistry registry=ConnectionStateAccessor.getPluginChannels(e.Connection.ClientChannel);registry.RegistrationCount++;ByteBuf buffer=Unpooled.wrappedBuffer(e.Payload);try{registry.readFrom(buffer);}finally{buffer.release();}if(registry.RegistrationCount!=2)return;registry.addRange(requiredPluginChannels);sendPluginMessage(e.Connection,"minecraft:register",registry.toPayload());session(e.Connection).syncSkins();sendNotice(e.Connection,"[Codexus] 成功注册 Channel ♪(^∇^*) ","green");}
    private static void handleAntiCheatMessage(EventPluginMessage e){ByteBuf envelope=Unpooled.wrappedBuffer(e.Payload);try{Integer id=readAntiCheatMessageId(envelope);if(id==null)return;IAntiCheatMessage message=AntiCheatMessageRegistry.createInbound(id);if(message==null)return;int length=readVarIntFromBuffer(envelope);if(length<0||length>envelope.readableBytes())throw new IllegalArgumentException("Invalid challenge payload length");ByteBuf payload=envelope.readSlice(length);message.read(payload,e.Connection);message.handle(e.Connection);}catch(RuntimeException ex){LOG.log(System.Logger.Level.WARNING,"Error handling Heypixel message",ex);}finally{envelope.release();}}
    public static Integer readAntiCheatMessageId(ByteBuf envelope){return readVarIntFromBuffer(envelope)==250?envelope.readInt():null;}
    private static void handleFloodgateForm(EventPluginMessage e){ByteBuf b=Unpooled.wrappedBuffer(e.Payload);try{b.readByte();byte first=b.readByte(),second=b.readByte();String form=DotNetEncoding.utf8(readByteArrayReadableBytes(b));if(form.contains("游戏信息收集与反作弊告知书"))sendPluginMessage(e.Connection,"floodgate:form",new byte[]{first,second,48});}finally{b.release();}}
    private static void handleServerboundPluginMessage(EventPluginMessage e){switch(e.Identifier){case "minecraft:brand"->e.Payload=Base64.getDecoder().decode("BWZvcmdl");case "minecraft:register"->e.IsCancelled=true;default->{}}}
    private static void sendPluginMessage(GameConnection c,String name,byte[] payload){CPacketPluginMessage packet=new CPacketPluginMessage();packet.Identifier=name;packet.Payload=payload;if(c.ServerChannel!=null)c.ServerChannel.writeAndFlush(packet);}
    private static AntiCheatSession session(GameConnection c){return ConnectionStateAccessor.getAntiCheatSession(c.ClientChannel,c);}
    private static long monotonicMillis(){return System.nanoTime()/1_000_000;}
    public static boolean isTargetGame(GameConnection c){return TargetGameId.equals(c.GameId);}
    @Override public synchronized void close(){for(Runnable action:unregister)action.run();unregister.clear();for(var future:timers)future.cancel(false);timers.clear();hookServer.close();initialized=false;if(instance==this)instance=null;}
}

