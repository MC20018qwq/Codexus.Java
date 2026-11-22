package codexus.heypixel.plugin.utilities;

import java.nio.file.*;
import java.util.zip.ZipFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
public final class ModArchiveMetadataReader {
    private ModArchiveMetadataReader(){}
    public static String readModsToml(String jarPath){Path p=Path.of(jarPath);if(!Files.isRegularFile(p))return null;try(ZipFile zip=new ZipFile(p.toFile())){var entry=zip.stream().filter(e->e.getName().replace('\\','/').equalsIgnoreCase("META-INF/mods.toml")).findFirst().orElse(null);if(entry==null)return null;try(var in=zip.getInputStream(entry)){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}catch(IOException e){throw new UncheckedIOException(e);}}
    public static String parseModId(String text){boolean section=false;for(String raw:text.split("\n")){String line=raw.strip();if(line.isEmpty()||line.startsWith("#"))continue;if(line.startsWith("[[mods]]")){section=true;continue;}if(line.startsWith("[[")&&!line.equals("[[mods]]")){section=false;continue;}if(!section||!line.contains("="))continue;String[] parts=line.split("=",2);if(!parts[0].strip().equalsIgnoreCase("modId"))continue;String value=parts[1].strip();int comment=value.indexOf('#');if(comment>=0)value=value.substring(0,comment).strip();return value.replaceAll("^\"+|\"+$","");}return null;}
    public static String replaceFileName(String originalPath,String fileName){int separator=Math.max(originalPath.lastIndexOf('/'),originalPath.lastIndexOf('\\'));return separator<0?fileName:originalPath.substring(0,separator+1)+fileName;}
}
