package codexus.heypixel.plugin.messages;

import java.util.*;
import java.util.function.Supplier;
public final class AntiCheatMessageRegistry {
    private AntiCheatMessageRegistry(){}
    private static final Map<Class<?>,Integer> OUT=new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<Integer,Supplier<? extends IAntiCheatMessage>> IN=new java.util.concurrent.ConcurrentHashMap<>();
    public static void registerDefaults(){OUT.put(AntiCheatReportMessage.class,1);OUT.put(KeepAliveMessage.class,2);OUT.put(CpsUpdateMessage.class,3);OUT.put(UseItemOnMessage.class,5);OUT.put(PlayerEntityInteractionMessage.class,6);IN.put(101,AntiCheatChallengeMessage::new);}
    public static IAntiCheatMessage createInbound(int id){var factory=IN.get(id);return factory==null?null:factory.get();}
    public static int getId(Class<?> type){Integer id=OUT.get(type);if(id==null)throw new IllegalArgumentException("Unknown C2S message type: "+type.getName());return id;}
}
