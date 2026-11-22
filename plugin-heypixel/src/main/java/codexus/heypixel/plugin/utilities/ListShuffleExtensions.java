package codexus.heypixel.plugin.utilities;

import codexus.csharpsup.DotNetRandom;
import java.util.*;
public final class ListShuffleExtensions {
    private ListShuffleExtensions(){}
    public static <T> void shuffle(List<T> values,DotNetRandom random){for(int i=values.size()-1;i>0;i--){int j=random.next(i+1);Collections.swap(values,i,j);}}
}
