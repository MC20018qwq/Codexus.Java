package codexus.development.sdk.handlers;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.utils.ChannelAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final GameConnection connection;
    public ClientHandler(GameConnection connection){this.connection=connection;}
    @Override public void channelActive(ChannelHandlerContext context){context.channel().attr(ChannelAttribute.Connection).set(connection);context.fireChannelActive();}
    @Override public void channelRead(ChannelHandlerContext context,Object message){connection.onServerReceived((ByteBuf)message);}
    @Override public void channelInactive(ChannelHandlerContext context){context.channel().attr(ChannelAttribute.Connection).set(null);connection.shutdown();context.fireChannelInactive();}
    @Override public void exceptionCaught(ChannelHandlerContext context,Throwable exception){System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"Remote Minecraft channel failed",exception);connection.shutdown();}
}
