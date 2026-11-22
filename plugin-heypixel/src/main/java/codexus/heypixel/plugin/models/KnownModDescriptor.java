package codexus.heypixel.plugin.models;

import com.fasterxml.jackson.annotation.JsonProperty;
public class KnownModDescriptor {
    @JsonProperty("mod_name") public String ModName="";
    @JsonProperty("mod_path") public String ModPath="";
    @JsonProperty("mod_hash") public String ModHash="";
    public KnownModDescriptor(){}
    public KnownModDescriptor(String name,String path,String hash){ModName=name;ModPath=path;ModHash=hash;}
    public KnownModDescriptor copy(){return new KnownModDescriptor(ModName,ModPath,ModHash);}
    @Override public String toString(){return "Name: "+ModName+", Path: "+ModPath+", Hash: "+ModHash;}
}
