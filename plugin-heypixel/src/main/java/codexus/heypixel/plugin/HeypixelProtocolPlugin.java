package codexus.heypixel.plugin;

import codexus.development.sdk.attributes.Plugin;
import codexus.development.sdk.plugin.IPlugin;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.event.EventLoginSuccess;
import codexus.development.sdk.manager.*;
import codexus.interceptors.event.EventHandshake;
import codexus.interceptors.packet.login.server.SPacketDisconnect;
import codexus.base12111.plugin.event.*;
import codexus.base12111.plugin.packet.play.client.CPacketPluginMessage;
import codexus.base12111.plugin.utils.*;
import codexus.base12111.plugin.utils.nbt.TextComponent;
import codexus.base12111.plugin.utils.patch.metadata.EntityDataSerializers;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.networking.*;
import codexus.heypixel.plugin.metadata.ReflectionMetadataRepository;
import codexus.heypixel.plugin.messages.*;
import codexus.csharpsup.DotNetEncoding;
import io.netty.buffer.*;
import io.netty.util.AttributeKey;
import java.util.*;
import java.util.concurrent.*;
import static codexus.base12111.plugin.extensions.AttributeExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@Plugin(id="F110DA9F-F0CB-F926-C72C-FEAC7FCF3601",name="Heypixel Protocol",description="Heypixel protocol transport for Minecraft 1.21.11",author="DevCodexus",version="3.0.0",dependencies={"FEF71E62-1DA0-4935-AE43-49102690B32F"})
public final class HeypixelProtocolPlugin implements IPlugin,AutoCloseable {
    public static final String TargetGameId="4661334467366178884",AntiCheatChannel="heypixel:s2cevent";
    private static final long PHASE_ORIGIN=monotonicMillis();
    private static final AttributeKey<Boolean> TIMERS=AttributeKey.valueOf("heypixel:timers-started");
    private static final System.Logger LOG=System.getLogger(HeypixelProtocolPlugin.class.getName());
    private static volatile HeypixelProtocolPlugin instance;
    // Loader capabilities belong to the real client; never invent an FML/NeoForge handshake.
    private static final List<String> requiredPluginChannels=List.of("worldedit:cui","storemod:buy","floodgate:custom","floodgate:packet",AntiCheatChannel,"report:areport","plugin:guild","floodgate:netease","floodgate:transfer","heypixel:onlinestats","floodgate:form","geckolib:main","floodgate:skin");
    private static final AttributeKey<EnumConnectionState> REGISTERED_PHASE=AttributeKey.valueOf("heypixel:registered-phase");
    private static final AttributeKey<Boolean> SKINS_SYNCED=AttributeKey.valueOf("heypixel:skins-synced");
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
        register(EventLoginSuccess.class,"channel_v12111",this::onLoginSuccess);register(EventPluginMessage.class,"base_12111",this::onPluginMessage);register(EventSwingArm.class,"base_12111",HeypixelProtocolPlugin::onSwingArm);register(EventGameJoin.class,"base_12111",HeypixelProtocolPlugin::onGameJoin);register(EventUseItemOn.class,"base_12111",HeypixelProtocolPlugin::onUseItemOn);register(EventUseItem.class,"base_12111",HeypixelProtocolPlugin::onUseItem);register(EventSetEntityMetadata.class,"base_12111",HeypixelProtocolPlugin::onEntityMetadata);register(EventHandshake.class,"channel_v12111",HeypixelProtocolPlugin::onHandshake);
        if(startHookListener) hookServer.startAsync().whenComplete((v,error)->{if(error!=null)LOG.log(System.Logger.Level.WARNING,"Cannot start local Heypixel hook listener",error);});initialized=true;
    }
    private <T extends IEventArgs> void register(Class<T> type,String channel,EventHandler<T> handler){EventManager.Instance.registerHandler(type,channel,handler);unregister.add(()->EventManager.Instance.unregisterHandler(type,channel,handler));}
    public static void sendNotice(GameConnection connection,String message,String color){
        if(connection.State!=EnumConnectionState.Play||connection.ClientChannel==null)return;
        var packet=new codexus.base12111.plugin.packet.play.server.simulation.SSystemChatMessage();
        packet.Message.Text=message;packet.Message.Color=color;
        packet.setClientProtocolVersion(connection.ProtocolVersion);
        connection.ClientChannel.writeAndFlush(packet);
    }
    private static void onEntityMetadata(EventSetEntityMetadata e){if(!isTargetGame(e.Connection))return;LocalPlayer player=getLocalPlayer(e.Connection);if(player==null)return;if(e.Packet.EntityId==player.PlayerId){int pose=EntityDataSerializers.getSerializedId(EntityDataSerializers.Pose);e.Packet.PackedItems.removeIf(item->item.serializerId()==pose);}}
    private static void onHandshake(EventHandshake e){
        if(e.IsCancelled||e.Handshake.NextState!=2||!isTargetGame(e.Connection)||e.Handshake.ProtocolVersion==774)return;
        GameConnection c=e.Connection;
        TextComponent reason=new TextComponent();
        reason.Text="[Codexus] Heypixel Protocol requires Minecraft 1.21.11 (protocol 774).";
        reason.Color="yellow";
        SPacketDisconnect packet=new SPacketDisconnect();packet.Reason=reason.toJson();
        e.IsCancelled=true;
        c.ClientChannel.writeAndFlush(packet).addListener(f->c.shutdown());
    }
    private static void onGameJoin(EventGameJoin e){
        if(!isTargetGame(e.Connection))return;
        registerChannelsForPhase(e.Connection);
        AntiCheatSession session=session(e.Connection);
        if(e.Connection.ClientChannel.attr(SKINS_SYNCED).setIfAbsent(true)==null)session.syncSkins();
        session.beginHandshake();session.sendInitialReport();
    }
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
    private void handleChannelRegistration(EventPluginMessage e){
        // Forward the server's advertisement unchanged; it is not a list of client capabilities.
        PluginChannelRegistry registry=ConnectionStateAccessor.getServerPluginChannels(e.Connection.ClientChannel);
        ByteBuf buffer=Unpooled.wrappedBuffer(e.Payload);
        try{registry.mergeFrom(buffer);}finally{buffer.release();}
        registerChannelsForPhase(e.Connection);
    }
    private static void registerChannelsForPhase(GameConnection connection){
        EnumConnectionState phase=connection.State;
        if(phase!=EnumConnectionState.Configuration&&phase!=EnumConnectionState.Play)return;
        if(connection.ServerChannel==null||connection.ClientChannel.attr(REGISTERED_PHASE).get()==phase)return;
        PluginChannelRegistry registry=ConnectionStateAccessor.getPluginChannels(connection.ClientChannel);
        registry.addRange(requiredPluginChannels);
        sendPluginMessage(connection,"minecraft:register",registry.toPayload());
        connection.ClientChannel.attr(REGISTERED_PHASE).set(phase);
    }
    private static void handleAntiCheatMessage(EventPluginMessage e){ByteBuf envelope=Unpooled.wrappedBuffer(e.Payload);try{Integer id=readAntiCheatMessageId(envelope);if(id==null)return;IAntiCheatMessage message=AntiCheatMessageRegistry.createInbound(id);if(message==null)return;int length=readVarIntFromBuffer(envelope);if(length<0||length>envelope.readableBytes())throw new IllegalArgumentException("Invalid challenge payload length");ByteBuf payload=envelope.readSlice(length);message.read(payload,e.Connection);message.handle(e.Connection);}catch(RuntimeException ex){LOG.log(System.Logger.Level.WARNING,"Error handling Heypixel message",ex);}finally{envelope.release();}}
    public static Integer readAntiCheatMessageId(ByteBuf envelope){return readVarIntFromBuffer(envelope)==250?envelope.readInt():null;}
    private static void handleFloodgateForm(EventPluginMessage e){ByteBuf b=Unpooled.wrappedBuffer(e.Payload);try{b.readByte();byte first=b.readByte(),second=b.readByte();String form=DotNetEncoding.utf8(readByteArrayReadableBytes(b));if(form.contains("游戏信息收集与反作弊告知书"))sendPluginMessage(e.Connection,"floodgate:form",new byte[]{first,second,48});}finally{b.release();}}
    private static void handleServerboundPluginMessage(EventPluginMessage e){
        // In particular, preserve minecraft:brand and all neoforge:* negotiation packets.
        if("minecraft:register".equals(e.Identifier)){
            PluginChannelRegistry registry=ConnectionStateAccessor.getPluginChannels(e.Connection.ClientChannel);
            ByteBuf buffer=Unpooled.wrappedBuffer(e.Payload);
            try{registry.mergeFrom(buffer);}finally{buffer.release();}
            registry.addRange(requiredPluginChannels);
            e.Payload=registry.toPayload();
            e.Connection.ClientChannel.attr(REGISTERED_PHASE).set(e.Connection.State);
        }else if("minecraft:unregister".equals(e.Identifier)){
            PluginChannelRegistry registry=ConnectionStateAccessor.getPluginChannels(e.Connection.ClientChannel);
            ByteBuf buffer=Unpooled.wrappedBuffer(e.Payload);
            try{registry.removeFrom(buffer);}finally{buffer.release();}
            // The proxy still implements its own channels even if the client unregisters them.
            registry.addRange(requiredPluginChannels);
            PluginChannelRegistry removed=new PluginChannelRegistry();
            buffer=Unpooled.wrappedBuffer(e.Payload);
            try{removed.readFrom(buffer);}finally{buffer.release();}
            removed.removeAll(requiredPluginChannels);
            e.Payload=removed.toPayload();
            e.IsCancelled=e.Payload.length==0;
        }
    }
    public static void sendPluginMessage(GameConnection connection,String name,byte[] payload){
        if(connection.ServerChannel==null)return;
        codexus.development.sdk.packet.IPacket packet;
        if(connection.State==EnumConnectionState.Configuration){
            var message=new codexus.base12111.plugin.packet.play.client.configuration.CPacketPluginMessage();
            message.Identifier=name;message.Payload=payload;packet=message;
        }else if(connection.State==EnumConnectionState.Play){
            var message=new CPacketPluginMessage();message.Identifier=name;message.Payload=payload;packet=message;
        }else{
            throw new IllegalStateException("Custom payload cannot be sent in "+connection.State);
        }
        packet.setClientProtocolVersion(connection.ProtocolVersion);
        connection.ServerChannel.writeAndFlush(packet);
    }
    private static AntiCheatSession session(GameConnection c){return ConnectionStateAccessor.getAntiCheatSession(c.ClientChannel,c);}
    private static long monotonicMillis(){return System.nanoTime()/1_000_000;}
    public static boolean isTargetGame(GameConnection c){return TargetGameId.equals(c.GameId);}
    @Override public synchronized void close(){for(Runnable action:unregister)action.run();unregister.clear();for(var future:timers)future.cancel(false);timers.clear();hookServer.close();initialized=false;if(instance==this)instance=null;}
}

