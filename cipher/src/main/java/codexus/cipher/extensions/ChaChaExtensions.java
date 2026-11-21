package codexus.cipher.extensions;

import codexus.cipher.connection.chacha.ChaChaOfSalsa;
import java.nio.*;
import java.util.*;
import java.util.zip.CRC32;

public final class ChaChaExtensions {
    private ChaChaExtensions() { }
    public static byte[] packMessage(ChaChaOfSalsa cipher, byte type, byte[] data) {
        if (data.length > 65527) throw new IllegalArgumentException("Authentication packet exceeds UInt16 framing.");
        byte[] packet = new byte[data.length + 10];
        ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN).putShort((short) (packet.length - 2));
        packet[6] = type;
        packet[7] = packet[8] = packet[9] = (byte) 136;
        System.arraycopy(data, 0, packet, 10, data.length);
        CRC32 crc = new CRC32(); crc.update(packet, 6, packet.length - 6);
        ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN).putInt(2, (int) crc.getValue());
        cipher.processBytes(packet, 2, packet.length - 2, packet, 2);
        return packet;
    }
    public record UnpackedMessage(byte type, byte[] data) { }
    public static UnpackedMessage unpackMessage(ChaChaOfSalsa cipher, byte[] data) {
        if (data.length < 8) throw new IllegalArgumentException("Authentication packet is truncated.");
        cipher.processBytes(data, 0, data.length, data, 0);
        CRC32 crc = new CRC32(); crc.update(data, 4, data.length - 4);
        int expected = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (expected != (int) crc.getValue()) throw new IllegalArgumentException("Unpacking failed");
        return new UnpackedMessage(data[4], Arrays.copyOfRange(data, 8, data.length));
    }
}
