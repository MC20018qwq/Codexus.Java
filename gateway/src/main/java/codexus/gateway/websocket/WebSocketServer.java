package codexus.gateway.websocket;

import codexus.gateway.handler.ClientHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Netty handles RFC 6455 framing; each client processes complete messages in arrival order. */
public final class WebSocketServer implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(WebSocketServer.class.getName());
    private final String path;
    private int port;
    private volatile boolean running;
    private EventLoopGroup acceptors, workers;
    private Channel listener;
    private final ConcurrentMap<UUID, Channel> clients = new ConcurrentHashMap<>();
    private final Set<ExecutorService> dispatchers = ConcurrentHashMap.newKeySet();
    private final ClientHandler handler;
    public WebSocketServer(int defaultPort, String path, ClientHandler handler) {
        if (defaultPort < 0 || defaultPort >= 65535) throw new IllegalArgumentException("Invalid WebSocket port");
        this.port = defaultPort; this.path = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        this.handler = Objects.requireNonNull(handler);
    }
    public int getPort() { return port; }
    public Channel getClient(UUID id) { return clients.get(id); }
    public CompletableFuture<Boolean> startAsync(boolean listenAll) {
        return CompletableFuture.supplyAsync(() -> start(listenAll), Thread::startVirtualThread);
    }
    private synchronized boolean start(boolean listenAll) {
        if (running) return false;
        acceptors = new NioEventLoopGroup(1); workers = new NioEventLoopGroup(2);
        var bootstrap = new ServerBootstrap().group(acceptors, workers).channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(65536), new SocketHandler());
                    }
                });
        while (port < 65535) {
            try {
                listener = bootstrap.bind(new InetSocketAddress(listenAll ? "0.0.0.0" : "127.0.0.1", port)).sync().channel();
                port = ((InetSocketAddress) listener.localAddress()).getPort(); running = true; return true;
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            catch (Exception e) {
                Throwable cause = e; while (cause.getCause() != null) cause = cause.getCause();
                if (!(cause instanceof BindException)) { LOG.log(System.Logger.Level.ERROR, "Unable to bind Gateway", e); break; }
                if (port == 0) break; port++;
            }
        }
        releaseGroups(); return false;
    }
    private final class SocketHandler extends SimpleChannelInboundHandler<Object> {
        private UUID id;
        private WebSocketServerHandshaker handshaker;
        private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("gateway-message-", 0).factory());
        SocketHandler() { dispatchers.add(dispatcher); }
        @Override protected void channelRead0(ChannelHandlerContext context, Object message) {
            if (message instanceof FullHttpRequest request) {
                String requestedPath = new QueryStringDecoder(request.uri()).path();
                if ((!requestedPath.equals(path) && !requestedPath.equals(path + "/"))
                        || !"websocket".equalsIgnoreCase(request.headers().get(HttpHeaderNames.UPGRADE))) {
                    context.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)).addListener(ChannelFutureListener.CLOSE); return;
                }
                var factory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaderNames.HOST) + path, null, true, 64 * 1024 * 1024);
                handshaker = factory.newHandshaker(request);
                if (handshaker == null) { WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(context.channel()); return; }
                handshaker.handshake(context.channel(), request).addListener(future -> {
                    if (!future.isSuccess()) { context.close(); return; }
                    context.pipeline().addBefore(context.name(), "complete-websocket-messages", new WebSocketFrameAggregator(64 * 1024 * 1024));
                    id = UUID.randomUUID(); clients.put(id, context.channel());
                    handler.onClientConnected(id, bytes -> {
                        if (context.channel().isActive()) context.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
                    }, reason -> context.writeAndFlush(new CloseWebSocketFrame(1000, reason)).addListener(ChannelFutureListener.CLOSE));
                });
            } else if (message instanceof CloseWebSocketFrame frame) {
                if (handshaker != null) handshaker.close(context.channel(), frame.retain()); else context.close();
            } else if (message instanceof PingWebSocketFrame frame) {
                context.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            } else if ((message instanceof BinaryWebSocketFrame || message instanceof TextWebSocketFrame) && id != null) {
                var frame = (WebSocketFrame) message; byte[] bytes = new byte[frame.content().readableBytes()]; frame.content().getBytes(frame.content().readerIndex(), bytes);
                dispatcher.execute(() -> handler.onMessageReceived(id, bytes));
            }
        }
        @Override public void channelInactive(ChannelHandlerContext context) {
            if (id != null) { clients.remove(id); handler.onClientDisconnected(id); }
            dispatcher.shutdownNow(); dispatchers.remove(dispatcher);
        }
        @Override public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            LOG.log(System.Logger.Level.WARNING, "Gateway WebSocket failed", cause); context.close();
        }
    }
    public CompletableFuture<Void> stopAsync() { return CompletableFuture.runAsync(this::close, Thread::startVirtualThread); }
    private void releaseGroups() {
        if (acceptors != null) acceptors.shutdownGracefully(0, 2, TimeUnit.SECONDS).syncUninterruptibly();
        if (workers != null) workers.shutdownGracefully(0, 2, TimeUnit.SECONDS).syncUninterruptibly();
        acceptors = null; workers = null;
    }
    @Override public synchronized void close() {
        running = false;
        if (listener != null) { listener.close().syncUninterruptibly(); listener = null; }
        clients.values().forEach(channel -> channel.close().syncUninterruptibly()); clients.clear();
        releaseGroups(); dispatchers.forEach(ExecutorService::shutdownNow); dispatchers.clear();
    }
}
