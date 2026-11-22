package codexus.heypixel.plugin.anticheat;

import java.util.ArrayDeque;
import java.util.function.LongSupplier;
public final class CpsTracker {
    private final ArrayDeque<Long> left=new ArrayDeque<>(),right=new ArrayDeque<>();
    private final LongSupplier clock;
    public CpsTracker(){this(System::currentTimeMillis);}
    public CpsTracker(LongSupplier clock){this.clock=clock;}
    public void recordLeftClick(int ignored){recordLeftClick();}
    public synchronized void recordLeftClick(){record(left);}
    public synchronized void recordRightClick(){record(right);}
    public synchronized int getLeftCps(){expire(left);return left.size();}
    public synchronized int getRightCps(){expire(right);return right.size();}
    public double getLeftClicksPerSecond(){return getLeftCps();}
    public double getRightClicksPerSecond(){return getRightCps();}
    public synchronized int countClicks(boolean isLeft,int interval){long cutoff=clock.getAsLong()-interval;return (int)(isLeft?left:right).stream().filter(t->t>=cutoff).count();}
    private void record(ArrayDeque<Long> queue){queue.add(clock.getAsLong());expire(queue);}
    private void expire(ArrayDeque<Long> queue){long cutoff=clock.getAsLong()-1000;while(!queue.isEmpty()&&queue.peek()<cutoff)queue.remove();}
}
