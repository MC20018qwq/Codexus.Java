package codexus.development.sdk.manager;
import codexus.development.sdk.connection.GameConnection;
public abstract class EventArgsBase implements IEventArgs {
    public GameConnection Connection;
    public boolean IsCancelled;
    protected EventArgsBase(GameConnection connection){Connection=connection;}
    public boolean isCancelled(){return IsCancelled;}
    public void setCancelled(boolean cancelled){IsCancelled=cancelled;}
}
