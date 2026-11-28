package codexus.heypixel.plugin.utilities;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class ModArchiveMetadataReader {
    private ModArchiveMetadataReader(){}
    private static final Pattern MOD_ID=Pattern.compile("^modId\\s*=\\s*([\"'])([a-z][a-z0-9_]*)\\1\\s*(?:#.*)?$",Pattern.CASE_INSENSITIVE);
    public static String readModsToml(String jarPath){
        Path path=Path.of(jarPath);
        if(!Files.isRegularFile(path))return null;
        try(ZipFile zip=new ZipFile(path.toFile())){
            for(String name:List.of("META-INF/neoforge.mods.toml","META-INF/mods.toml")){
                var entry=zip.stream().filter(e->e.getName().replace('\\','/').equalsIgnoreCase(name)).findFirst().orElse(null);
                if(entry==null)continue;
                try(var input=zip.getInputStream(entry)){return new String(input.readAllBytes(),StandardCharsets.UTF_8);}
            }
            return null;
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    public static String parseModId(String text){var ids=parseModIds(text);return ids.isEmpty()?null:ids.getFirst();}
    /** A NeoForge archive can contain multiple [[mods]] declarations. */
    public static List<String> parseModIds(String text){
        if(text==null)return List.of();
        boolean section=false;Set<String> ids=new LinkedHashSet<>();
        for(String raw:text.split("\\R")){
            String line=raw.replace("\uFEFF", "").strip();
            if(line.startsWith("[")){section=line.matches("\\[\\[mods]]\\s*(?:#.*)?");continue;}
            if(!section)continue;
            var match=MOD_ID.matcher(line);
            if(match.matches())ids.add(match.group(2));
        }
        return List.copyOf(ids);
    }
    public static String replaceFileName(String originalPath,String fileName){int separator=Math.max(originalPath.lastIndexOf('/'),originalPath.lastIndexOf('\\'));return separator<0?fileName:originalPath.substring(0,separator+1)+fileName;}
}
