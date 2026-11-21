package codexus.development.sdk.packet;
import codexus.development.sdk.enums.*;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @Repeatable(RegisterPacket.List.class)
public @interface RegisterPacket {
    EnumConnectionState state();
    EnumPacketDirection direction();
    int[] packetIds();
    EnumProtocolVersion[] versions() default {};
    boolean skip() default false;
    boolean append() default false;
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    @interface List { RegisterPacket[] value(); }
}
