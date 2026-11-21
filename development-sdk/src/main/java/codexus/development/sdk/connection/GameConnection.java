package codexus.development.sdk.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.Socks5ProxyHandler;
import codexus.development.sdk.analysis.*;
import codexus.development.sdk.entities.*;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.event.*;
import codexus.development.sdk.handlers.ClientHandler;
import codexus.development.sdk.manager.*;
import codexus.development.sdk.packet.*;
import codexus.development.sdk.utils.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;

public class GameConnection implements IConnection {
    public final Channel ClientChannel;
    private EventLoopGroup workerGroup;
    private final AtomicBoolean prepared=new AtomicBoolean(),closed=new AtomicBoolean();
    public volatile Channel ServerChannel;
    public final InterceptorConfig Config;
    public String NickName;
    public EnumProtocolVersion ProtocolVersion=EnumProtocolVersion.None;
    public EnumConnectionState State=EnumConnectionState.Handshaking;
    private int rawProtocolVersion = -1;
    private int rawState;
    public void setProtocolVersionValue(int value) {
        rawProtocolVersion = value;
        ProtocolVersion = EnumProtocolVersion.fromValue(value);
    }
    public int getProtocolVersionValue() { return ProtocolVersion == null ? rawProtocolVersion : ProtocolVersion.value(); }
    public void setStateValue(int value) {
        rawState = value;
        State = EnumConnectionState.fromValue(value);
    }
    public int getStateValue() { return State == null ? rawState : State.value(); }
    public BiConsumer<InterceptorConfig,String> OnJoinServer;
    public final EventLoopGroup TaskGroup=new NioEventLoopGroup(1);
    public GameSession Session;
    public final String GameId,ModInfo,ForwardAddress;
    public final int ForwardPort;
    public byte[] Uuid=new byte[16];
    public GameConnection(InterceptorConfig config,Channel channel){Config=config;ClientChannel=channel;NickName=config.NickName;OnJoinServer=config.OnJoinServer;Session=new GameSession(config.NickName,config.UserId,config.UserToken);GameId=config.GameId;ModInfo=config.ModInfo;ForwardPort=config.ForwardPort;ForwardAddress=config.ForwardAddress;}
    public GameConnection(EntitySocks5 socks,String modInfo,String gameId,String forwardAddress,int forwardPort,String nickName,String userId,String userToken,Channel channel,Consumer<String> onJoinServer){this(config(socks,modInfo,gameId,forwardAddress,forwardPort,nickName,userId,userToken,onJoinServer),channel);}
    private static InterceptorConfig config(EntitySocks5 socks,String modInfo,String gameId,String address,int port,String nickName,String userId,String token,Consumer<String> onJoin){var c=new InterceptorConfig();c.IsRental=false;c.LocalAddress="127.0.0.1";c.LocalPort=0;c.NickName=nickName;c.ForwardAddress=address;c.ForwardPort=port;c.ServerName="";c.ServerVersion="";c.ModInfo=modInfo;c.GameId=gameId;c.UserId=userId;c.UserToken=token;c.OnJoinServer=onJoin==null?null:(cfg,hash)->onJoin.accept(hash);c.Socks5=socks;return c;}
    @Override public void prepare(){
        if(!prepared.compareAndSet(false,true))throw new IllegalStateException("Connection has already been prepared.");
        if(closed.get())return;
        workerGroup=new NioEventLoopGroup(1);
        try {
            Bootstrap b=new Bootstrap().group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY,true).option(ChannelOption.SO_KEEPALIVE,true).option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT).option(ChannelOption.SO_SNDBUF,1048576).option(ChannelOption.SO_RCVBUF,1048576).option(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(524288,1048576)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,30000).handler(new ChannelInitializer<Channel>(){
                @Override protected void initChannel(Channel channel)throws Exception{
                    if(Config.Socks5.Enabled)channel.pipeline().addLast("socks5",new Socks5ProxyHandler(new InetSocketAddress(resolveAddress(Config.Socks5.Address),Config.Socks5.Port),Config.Socks5.Username,Config.Socks5.Password));
                    channel.pipeline().addLast("splitter",new MessageDeserializer21Bit()).addLast("handler",new ClientHandler(GameConnection.this)).addLast("pre-encoder",new MessageSerializer21Bit()).addLast("encoder",new MessageSerializer());
                }
            });
            EventParseAddress target=EventManager.Instance.triggerEvent(MessageChannels.ChannelConnection,new EventParseAddress(this,ForwardAddress,ForwardPort));
            ServerChannel=b.connect(resolveAddress(target.Address),target.Port).syncUninterruptibly().channel();
            if(closed.get()||!ClientChannel.isActive()){ServerChannel.close();return;}
        }catch(Exception e){if(closed.get())return;System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"Failed to prepare Minecraft connection to "+ForwardAddress+":"+ForwardPort,e);ServerChannel=null;}
        if(ServerChannel==null)shutdown();
    }
    public void onServerReceived(ByteBuf buffer){handlePacketReceived(buffer,EnumPacketDirection.ClientBound,data->ClientChannel.writeAndFlush(data));}
    public void onClientReceived(ByteBuf buffer){handlePacketReceived(buffer,EnumPacketDirection.ServerBound,data->{if(ServerChannel!=null)ServerChannel.writeAndFlush(data);else io.netty.util.ReferenceCountUtil.release(data);});}
    public void shutdown(){if(!closed.compareAndSet(false,true))return;EventManager.Instance.triggerEvent(MessageChannels.ChannelConnection,new EventConnectionClosed(this));TaskGroup.shutdownGracefully();ClientChannel.close();if(ServerChannel!=null)ServerChannel.close();if(workerGroup!=null)workerGroup.shutdownGracefully();}
    private void handlePacketReceived(ByteBuf buffer,EnumPacketDirection direction,Consumer<Object> redirect){
        boolean forwarded=false;
        try {
            buffer.markReaderIndex();int id=readVarIntFromBuffer(buffer),payloadIndex=buffer.readerIndex();
            IPacket packet=PacketManager.Instance.buildPacket(State,direction,ProtocolVersion,id);var extra=PacketManager.Instance.buildAdditionalPackets(State,direction,ProtocolVersion,id);
            if(packet==null&&extra.isEmpty()){buffer.resetReaderIndex();redirect.accept(buffer);forwarded=true;return;}
            boolean original=packet==null||PacketManager.Instance.getMetadata(packet).skip();
            if(!original&&handleRegisteredPacket(buffer,payloadIndex,packet)){buffer.resetReaderIndex();return;}
            for(IPacket p:extra)if(!PacketManager.Instance.getMetadata(p).skip()&&handleRegisteredPacket(buffer,payloadIndex,p)){buffer.resetReaderIndex();return;}
            buffer.resetReaderIndex();redirect.accept(original?buffer:packet);forwarded=original;
        }finally{if(!forwarded)buffer.release();}
    }
    private boolean handleRegisteredPacket(ByteBuf buffer,int payloadIndex,IPacket packet){buffer.readerIndex(payloadIndex);packet.setClientProtocolVersion(ProtocolVersion);packet.readFromBuffer(buffer);return packet.handlePacket(this);}
    public static void enableCompression(Channel channel,int threshold){
        var pipeline=channel.pipeline();
        if(threshold<0){if(pipeline.get("decompress") instanceof NettyCompressionDecoder)pipeline.remove("decompress");if(pipeline.get("compress") instanceof NettyCompressionEncoder)pipeline.remove("compress");}
        else {if(pipeline.get("decompress") instanceof NettyCompressionDecoder d)d.Threshold=threshold;else pipeline.addAfter("splitter","decompress",new NettyCompressionDecoder(threshold));if(pipeline.get("compress") instanceof NettyCompressionEncoder e)e.Threshold=threshold;else pipeline.addBefore("encoder","compress",new NettyCompressionEncoder(threshold));}
    }
    public static void enableEncryption(Channel channel,byte[] key){channel.pipeline().addBefore("splitter","decrypt",new NettyEncryptionDecoder(key)).addBefore("pre-encoder","encrypt",new NettyEncryptionEncoder(key));}
    public static InetAddress resolveAddress(String address)throws UnknownHostException{InetAddress[] all=InetAddress.getAllByName(address);return Arrays.stream(all).filter(a->a instanceof Inet4Address).findFirst().orElse(all[0]);}
}
