package codexus.development.sdk.manager;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import java.util.*;
public class PacketManager {
    public static final PacketManager Instance=new PacketManager();
    private record Key(EnumConnectionState state,EnumPacketDirection direction,EnumProtocolVersion version,int id) {}
    private final Map<Class<?>,Map<EnumProtocolVersion,Integer>> ids=new HashMap<>();
    private final Map<Class<?>,EnumConnectionState> states=new HashMap<>();
    private final Map<Class<?>,RegisterPacket> metadata=new HashMap<>();
    private final Map<Key,Class<? extends IPacket>> packets=new HashMap<>();
    private final Map<Key,List<Class<? extends IPacket>>> additional=new HashMap<>();
    private boolean registered;
    private PacketManager() { registerDefaultPackets(); }
    public void registerDefaultPackets(){registerPacketClasses(codexus.development.sdk.utils.ClassCatalog.classPathClasses());registered=true;}
    @SuppressWarnings("unchecked") public void registerPacketClasses(Iterable<Class<?>> classes){
        for(Class<?> type:classes)if(IPacket.class.isAssignableFrom(type)&&!type.isInterface()&&!java.lang.reflect.Modifier.isAbstract(type.getModifiers()))
            for(RegisterPacket item:type.getDeclaredAnnotationsByType(RegisterPacket.class))registerPacket(item,(Class<? extends IPacket>)type);
    }
    public void registerPacket(RegisterPacket item,Class<? extends IPacket> type){
        try{type.getConstructor();}catch(NoSuchMethodException e){throw new IllegalArgumentException("Type '"+type.getName()+"' does not have a parameterless constructor.",e);}
        states.put(type,item.state());metadata.put(type,item);var idsByVersion=ids.computeIfAbsent(type,t->new HashMap<>());
        EnumProtocolVersion[] versions=item.versions().length==0?EnumProtocolVersion.values():item.versions();
        for(int i=0;i<versions.length;i++){int id=item.packetIds()[Math.min(item.packetIds().length-1,i)];idsByVersion.put(versions[i],id);Key key=new Key(item.state(),item.direction(),versions[i],id);
            if(item.append()){var types=additional.computeIfAbsent(key,k->new ArrayList<>());if(!types.contains(type))types.add(type);}else packets.put(key,type);
        }
    }
    public IPacket buildPacket(EnumConnectionState state,EnumPacketDirection direction,EnumProtocolVersion version,int id){return createPacket(packets.get(new Key(state,direction,version,id)));}
    public List<IPacket> buildAdditionalPackets(EnumConnectionState state,EnumPacketDirection direction,EnumProtocolVersion version,int id){var result=new ArrayList<IPacket>();for(var type:additional.getOrDefault(new Key(state,direction,version,id),List.of())){var p=createPacket(type);if(p!=null)result.add(p);}return result;}
    private IPacket createPacket(Class<? extends IPacket> type){if(type==null)return null;try{return type.getConstructor().newInstance();}catch(ReflectiveOperationException e){System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"Failed to build packet "+type,e);return null;}}
    public int getPacketId(EnumProtocolVersion version,IPacket packet){return ids.getOrDefault(packet.getClass(),Map.of()).getOrDefault(version,-1);}
    public RegisterPacket getMetadata(IPacket packet){return metadata.get(packet.getClass());}
    public EnumConnectionState getState(IPacket packet){return states.getOrDefault(packet.getClass(),EnumConnectionState.Handshaking);}
    public void ensureRegistered(){if(!registered)throw new IllegalStateException("Should never call CheckIsRegistered()");}
}
