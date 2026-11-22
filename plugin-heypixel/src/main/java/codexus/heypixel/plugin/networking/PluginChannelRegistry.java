package codexus.heypixel.plugin.networking;

import io.netty.buffer.ByteBuf;
import codexus.csharpsup.DotNetEncoding;
import java.io.ByteArrayOutputStream;
import java.util.*;
import static codexus.development.sdk.extensions.NettyExtensions.readByteArrayReadableBytes;
public final class PluginChannelRegistry {
    private final Set<String> channels=new LinkedHashSet<>();
    public int RegistrationCount;
    public synchronized PluginChannelRegistry readFrom(ByteBuf buffer){channels.clear();for(String channel:DotNetEncoding.utf8(readByteArrayReadableBytes(buffer)).split("\u0000",-1))if(!channel.isEmpty())channels.add(channel);return this;}
    public synchronized void addRange(Iterable<String> additional){for(String channel:additional)channels.add(channel);}
    public synchronized byte[] toPayload(){ByteArrayOutputStream output=new ByteArrayOutputStream();for(String channel:channels){output.writeBytes(DotNetEncoding.utf8(channel));output.write(0);}return output.toByteArray();}
    @Override public synchronized String toString(){return String.join(",",channels);}
}
