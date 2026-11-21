package codexus.development.sdk.connection;
import io.netty.buffer.ByteBuf;
public interface IConnection {void prepare();void onServerReceived(ByteBuf buffer);void onClientReceived(ByteBuf buffer);void shutdown();}
