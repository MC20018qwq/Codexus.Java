package codexus.development.sdk.event;
import codexus.development.sdk.manager.IEventArgs;
public class EventCreateInterceptor implements IEventArgs {public int Port;public boolean IsCancelled;public EventCreateInterceptor(int port){Port=port;}public boolean isCancelled(){return IsCancelled;}public void setCancelled(boolean value){IsCancelled=value;}}
