package codexus.heypixel.plugin.models;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.Objects;
public final class Uuid128 {
    public final long MostSignificantBits,LeastSignificantBits;
    public Uuid128(long most,long least){MostSignificantBits=most;LeastSignificantBits=least;}
    public Uuid128(byte[] bytes){Objects.requireNonNull(bytes);if(bytes.length!=16)throw new IllegalArgumentException("data must be 16 bytes in length");ByteBuffer b=ByteBuffer.wrap(bytes);MostSignificantBits=b.getLong();LeastSignificantBits=b.getLong();}
    public Uuid128(String value){String hex=value.replace("-","");MostSignificantBits=Long.parseUnsignedLong(hex.substring(0,16),16);LeastSignificantBits=Long.parseUnsignedLong(hex.substring(16,32),16);}
    public String toEncodedString(){return LeastSignificantBits+"|-|"+MostSignificantBits;}
    public static Uuid128 fromEncodedString(String value){String[] parts=value.split("\\|-[|]",-1);if(parts.length!=2)throw new UuidFormatException("Invalid encoded UUID format");return new Uuid128(Long.parseLong(parts[1].strip()),Long.parseLong(parts[0].strip()));}
    public static Uuid128 parse(String value){if(value==null||value.isBlank())throw new UuidFormatException("UUID string cannot be null or empty");String s=value.strip();if(!s.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))throw new UuidFormatException("Invalid UUID format");return new Uuid128(s);}
    public static Uuid128 newRandom(){UUID u=UUID.randomUUID();return new Uuid128(u.getMostSignificantBits(),u.getLeastSignificantBits());}
    public UUID toUuid(){return new UUID(MostSignificantBits,LeastSignificantBits);}
    @Override public String toString(){return toUuid().toString();}
}
