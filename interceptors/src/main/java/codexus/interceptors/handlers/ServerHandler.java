package codexus.interceptors.handlers;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.entities.InterceptorConfig;
import codexus.development.sdk.utils.ChannelAttribute;
import codexus.interceptors.Interceptor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final Interceptor interceptor;
    private final InterceptorConfig config;
    public ServerHandler(Interceptor interceptor, InterceptorConfig config) {
        this.interceptor = interceptor;
        this.config = config;
    }
    @Override public void channelActive(ChannelHandlerContext context) {
        interceptor.ActiveChannels.putIfAbsent(context.channel().id(), context.channel());
        var connection = new GameConnection(config.clone(), context.channel());
        context.channel().attr(ChannelAttribute.Connection).set(connection);
        connection.prepare();
        if (connection.ServerChannel != null) context.fireChannelActive();
    }
    @Override public void channelRead(ChannelHandlerContext context, Object message) {
        context.channel().attr(ChannelAttribute.Connection).get().onClientReceived((ByteBuf) message);
    }
    @Override public void channelInactive(ChannelHandlerContext context) {
        interceptor.ActiveChannels.remove(context.channel().id());
        var connection = context.channel().attr(ChannelAttribute.Connection).getAndSet(null);
        if (connection != null) connection.shutdown();
        context.fireChannelInactive();
    }
    @Override public void exceptionCaught(ChannelHandlerContext context, Throwable exception) {
        System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,
                "Minecraft client channel failed: " + context.channel().remoteAddress(), exception);
        context.close();
    }
}
