package codexus.cipher.utils.http;
import java.util.*;
import java.util.stream.Collectors;
import codexus.csharpsup.DotNetUri;
public final class ParameterBuilder {
    private final Map<String,String> parameters=new codexus.csharpsup.DotNetDictionary<>();
    public String Url;
    public ParameterBuilder(){}
    public ParameterBuilder(String parameter){
        int query=parameter.indexOf('?');if(query>=0){Url=parameter.substring(0,query);parameter=parameter.substring(query+1);}
        for(String item:parameter.split("&",-1)){String[] pair=item.split("=",-1);if(pair.length==2){if(parameters.containsKey(pair[0]))throw new IllegalArgumentException("An item with the same key has already been added.");parameters.put(pair[0],pair[1]);}}
    }
    public String get(String key){return parameters.getOrDefault(key,"");}
    public ParameterBuilder append(String key,String value){parameters.put(Objects.requireNonNull(key),value);return this;}
    public ParameterBuilder remove(String key){parameters.remove(Objects.requireNonNull(key));return this;}
    public String formUrlEncode(){return parameters.entrySet().stream().map(e->DotNetUri.escapeDataString(e.getKey())+"="+DotNetUri.escapeDataString(e.getValue())).collect(Collectors.joining("&"));}
    public String toQueryUrl(){return (Url==null?"":Url)+"?"+formUrlEncode();}
    @Override public String toString(){return parameters.entrySet().stream().map(e->e.getKey()+"="+(e.getValue()==null?"":e.getValue())).collect(Collectors.joining("&"));}
}
