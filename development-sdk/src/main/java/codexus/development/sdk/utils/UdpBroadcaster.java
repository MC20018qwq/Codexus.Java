package codexus.development.sdk.utils;

import codexus.csharpsup.DotNetEncoding;
import java.io.*;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.*;

public class UdpBroadcaster implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(UdpBroadcaster.class.getName());
    private final boolean legacy;
    private final String roleName, serverIp;
    private final int serverPort;
    private final InetSocketAddress target;
    private final List<DatagramChannel> clients;
    private volatile BroadcastRun currentRun;
    private static final class BroadcastRun {
        volatile boolean cancelled;
        Thread thread;
    }
    public UdpBroadcaster(String multicastAddress, int port, int targetPort, String serverIp,
                          String roleName, boolean is189Protocol) {
        try {
            InetAddress address = InetAddress.getByName(multicastAddress);
            if (!(address instanceof Inet4Address)) throw new IllegalArgumentException("Expected IPv4 broadcast address");
            target = new InetSocketAddress(address, port);
            clients = address.isMulticastAddress() ? createMulticastClients() : new ArrayList<>();
            if (!address.isMulticastAddress()) {
                DatagramChannel channel = createUdpClient();
                channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
                clients.add(channel);
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        serverPort = targetPort;
        legacy = is189Protocol;
        this.serverIp = serverIp;
        this.roleName = roleName;
    }
    public CompletableFuture<Void> startBroadcastingAsync() {
        BroadcastRun run = new BroadcastRun();
        currentRun = run;
        CompletableFuture<Void> result = new CompletableFuture<>();
        run.thread = Thread.ofVirtual().name("codexus-lan-broadcast").unstarted(() -> {
            try {
                while (!currentRun.cancelled) {
                    sendMessage();
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                if (!currentRun.cancelled) Thread.currentThread().interrupt();
            } catch (Exception e) {
                if (!currentRun.cancelled) LOG.log(System.Logger.Level.ERROR, "UDP broadcast error", e);
            } finally { result.complete(null); }
        });
        run.thread.start();
        return result;
    }
    private void sendMessage() {
        try {
            byte[] bytes = DotNetEncoding.utf8(buildMessage());
            for (DatagramChannel channel : clients) {
                try { channel.send(java.nio.ByteBuffer.wrap(bytes), target); }
                catch (NoRouteToHostException e) {
                    LOG.log(System.Logger.Level.DEBUG, "UDP multicast interface is temporarily unreachable", e);
                }
            }
        } catch (Exception e) {
            if (currentRun == null || !currentRun.cancelled) LOG.log(System.Logger.Level.ERROR, "UDP send failed", e);
        }
    }
    private String buildMessage() {
        return "[MOTD] " + (legacy ? "Codexus " : "§bCodexus §f") + serverIp + " -> " + roleName
                + "[/MOTD][AD]" + serverPort + "[/AD]";
    }
    public void stop() {
        BroadcastRun run = currentRun;
        if (run != null) {
            run.cancelled = true;
            if (run.thread != null) run.thread.interrupt();
        }
    }
    @Override public void close() {
        stop();
        for (DatagramChannel channel : clients) {
            try { channel.close(); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    private static DatagramChannel createUdpClient() throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        return channel;
    }
    private static List<DatagramChannel> createMulticastClients() throws IOException {
        Set<InetAddress> addresses = new HashSet<>();
        List<DatagramChannel> result = new ArrayList<>();
        for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!network.isUp() || !network.supportsMulticast() || network.isLoopback()) continue;
            for (InetAddress address : Collections.list(network.getInetAddresses())) {
                if (!(address instanceof Inet4Address) || address.isLoopbackAddress() || address.isAnyLocalAddress()
                        || !addresses.add(address)) continue;
                DatagramChannel channel = null;
                try {
                    channel = createUdpClient();
                    channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
                    channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1);
                    channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, network);
                    result.add(channel);
                } catch (IOException e) {
                    if (channel != null) channel.close();
                    LOG.log(System.Logger.Level.DEBUG, "Cannot configure UDP multicast interface " + address, e);
                }
            }
        }
        if (result.isEmpty()) {
            DatagramChannel channel = createUdpClient();
            channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1);
            result.add(channel);
        }
        return result;
    }
}
