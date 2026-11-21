package codexus.csharpsup;
import com.fasterxml.jackson.annotation.*;
public final class DotNetVersion implements Comparable<DotNetVersion> {
    public final int Major, Minor, Build, Revision;
    public DotNetVersion(int major, int minor) { this(major, minor, -1, -1); }
    public DotNetVersion(int major, int minor, int build, int revision) {
        if(major<0||minor<0||build< -1||revision< -1||build==-1&&revision!=-1) throw new IllegalArgumentException("Invalid version component");
        Major=major;Minor=minor;Build=build;Revision=revision;
    }
    @JsonCreator public static DotNetVersion parse(String value) {
        String[] p=value.split("\\.",-1); if(p.length<2||p.length>4) throw new IllegalArgumentException("Version requires 2 to 4 components");
        int[] n={0,0,-1,-1};for(int i=0;i<p.length;i++){n[i]=Integer.parseInt(p[i].trim());if(n[i]<0)throw new IllegalArgumentException("Negative version component");}return new DotNetVersion(n[0],n[1],n[2],n[3]);
    }
    @JsonValue @Override public String toString() { return Major+"."+Minor+(Build<0?"":"."+Build)+(Revision<0?"":"."+Revision); }
    @Override public int compareTo(DotNetVersion v){int[] a={Major,Minor,Build,Revision},b={v.Major,v.Minor,v.Build,v.Revision};for(int i=0;i<4;i++){int c=Integer.compare(a[i],b[i]);if(c!=0)return c;}return 0;}
    @Override public boolean equals(Object o){return o instanceof DotNetVersion v&&compareTo(v)==0;}
    @Override public int hashCode(){return java.util.Objects.hash(Major,Minor,Build,Revision);}
}
