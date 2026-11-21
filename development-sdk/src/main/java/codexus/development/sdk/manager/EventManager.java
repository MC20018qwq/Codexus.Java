package codexus.development.sdk.manager;
import java.util.*;
public class EventManager {
    public static final EventManager Instance=new EventManager();
    private final Map<Class<?>,Map<String,SortedMap<Integer,List<EventHandler<?>>>>> handlers=new HashMap<>();
    public <T extends IEventArgs> void registerHandler(Class<T> type,String channel,EventHandler<T> handler){registerHandler(type,channel,handler,0);}
    public <T extends IEventArgs> void registerHandler(Class<T> type,String channel,EventHandler<T> handler,int priority) {
        Objects.requireNonNull(handler);if(channel==null||channel.isEmpty())throw new IllegalArgumentException("channel");
        handlers.computeIfAbsent(type,t->new HashMap<>()).computeIfAbsent(channel,c->new TreeMap<>(Comparator.reverseOrder())).computeIfAbsent(priority,p->new ArrayList<>()).add(handler);
    }
    public <T extends IEventArgs> boolean unregisterHandler(Class<T> type,String channel,EventHandler<T> handler) {
        if(channel==null||channel.isEmpty()||handler==null)return false;
        var channels=handlers.get(type);if(channels==null)return false;var priorities=channels.get(channel);if(priorities==null)return false;
        for(int priority:new ArrayList<>(priorities.keySet())){var list=priorities.get(priority);if(list.remove(handler)){if(list.isEmpty())priorities.remove(priority);if(priorities.isEmpty())channels.remove(channel);if(channels.isEmpty())handlers.remove(type);return true;}}return false;
    }
    public <T extends IEventArgs> T triggerEvent(String channel,T args){return triggerEvent(new String[]{channel},args);}
    @SuppressWarnings("unchecked") public <T extends IEventArgs> T triggerEvent(String[] channels,T args){
        Objects.requireNonNull(args);
        for(String channel:channels){var map=handlers.get(args.getClass());if(map==null)continue;var priorities=map.get(channel);if(priorities==null)continue;
            for(var list:priorities.values())for(var handler:new ArrayList<>(list))((EventHandler<T>)handler).handle(args);
        }return args;
    }
    public boolean hasHandlersForEvent(Class<? extends IEventArgs> type,String channel){if(channel==null||channel.isEmpty())return false;var map=handlers.get(type);return map!=null&&map.containsKey(channel)&&map.get(channel).values().stream().anyMatch(l->!l.isEmpty());}
    public void clearAllHandlers(){handlers.clear();}
    public void clearHandlersForEventType(Class<? extends IEventArgs> type){handlers.remove(type);}
    public void clearHandlersForEvent(Class<? extends IEventArgs> type,String channel){if(channel==null||channel.isEmpty())return;var map=handlers.get(type);if(map!=null){map.remove(channel);if(map.isEmpty())handlers.remove(type);}}
}
