package codexus.heypixel.plugin.networking;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.concurrent.*;
import codexus.heypixel.plugin.anticheat.AntiCheatSession;
public final class LocalHttpListenerServer implements AutoCloseable {
    private HttpServer listener;
    private ExecutorService executor;
    public synchronized CompletableFuture<Void> startAsync(){try{start(23233);return CompletableFuture.completedFuture(null);}catch(IOException e){return CompletableFuture.failedFuture(e);}}
    public synchronized void start(int port)throws IOException {if(listener!=null)throw new IllegalStateException("Listener already started");HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",port),0);ExecutorService workers=Executors.newVirtualThreadPerTaskExecutor();server.setExecutor(workers);server.createContext("/",exchange->{try{if(exchange.getRequestURI().getPath().contains("setHook"))AntiCheatSession.IsLocalHookConfigured=true;byte[] body="Method not supported.".getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(405,body.length);exchange.getResponseBody().write(body);}finally{exchange.close();}});listener=server;executor=workers;server.start();}
    public synchronized int getPort(){if(listener==null)throw new IllegalStateException("Listener not started");return listener.getAddress().getPort();}
    public void stop(){close();}
    @Override public synchronized void close(){if(listener!=null){listener.stop(0);listener=null;}if(executor!=null){executor.shutdownNow();executor=null;}}
}
