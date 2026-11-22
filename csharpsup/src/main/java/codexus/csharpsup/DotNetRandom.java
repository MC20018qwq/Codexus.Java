package codexus.csharpsup;

/** Compatibility for the seeded System.Random(int) subtractive generator. Not cryptographic. */
public final class DotNetRandom {
    private final int[] state=new int[56];
    private int first,second=21;
    public DotNetRandom(int seed){
        int value=161803398-(seed==Integer.MIN_VALUE?Integer.MAX_VALUE:Math.abs(seed));
        state[55]=value; int next=1;
        for(int i=1;i<55;i++){int slot=(21*i)%55;state[slot]=next;next=value-next;if(next<0)next+=Integer.MAX_VALUE;value=state[slot];}
        for(int k=0;k<4;k++)for(int i=1;i<56;i++){state[i]-=state[1+(i+30)%55];if(state[i]<0)state[i]+=Integer.MAX_VALUE;}
    }
    public int next(){if(++first>=56)first=1;if(++second>=56)second=1;int value=state[first]-state[second];if(value==Integer.MAX_VALUE)value--;if(value<0)value+=Integer.MAX_VALUE;state[first]=value;return value;}
    public void nextBytes(byte[] bytes){for(int i=0;i<bytes.length;i++)bytes[i]=(byte)(next()%256);}
    public double nextDouble(){return next()*(1.0/Integer.MAX_VALUE);}
    public int next(int maximum){if(maximum<0)throw new IllegalArgumentException("maximum");return (int)(nextDouble()*maximum);}
    public int next(int minimum,int maximum){if(minimum>maximum)throw new IllegalArgumentException("minimum > maximum");long range=(long)maximum-minimum;if(range<=Integer.MAX_VALUE)return (int)(nextDouble()*range)+minimum;int sample=next();if(next()%2==0)sample=-sample;double value=sample;value+=Integer.MAX_VALUE-1;value/=2L*Integer.MAX_VALUE-1;return (int)((long)(value*range)+minimum);}
}
