package codexus.heypixel.plugin.metadata;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import codexus.heypixel.plugin.utilities.HashExtensions;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
public final class ReflectionMetadataRepository {
    private ReflectionMetadataRepository(){}
    private static final System.Logger LOG=System.getLogger(ReflectionMetadataRepository.class.getName());
    private static final ObjectMapper JSON=JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES).enable(JsonParser.Feature.ALLOW_COMMENTS).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
    private static final TypeReference<Map<String,List<EnumMetadata>>> TYPE=new TypeReference<>(){};
    private static volatile Map<String,List<EnumMetadata>> mappings=Map.of();
    private static boolean initialized;
    public static synchronized void initialize(){if(initialized)return;Map<String,List<EnumMetadata>> loaded=readDirectory(ReflectionMetadataPaths.CacheDirectory);if(loaded.isEmpty())loaded=readBundled();mappings=Collections.unmodifiableMap(loaded);initialized=true;}
    public static synchronized void loadMappings(Path directory){Map<String,List<EnumMetadata>> loaded=readDirectory(directory);mappings=Collections.unmodifiableMap(loaded);initialized=true;}
    public static synchronized void loadBundledMappings(){mappings=Collections.unmodifiableMap(readBundled());initialized=true;}
    private static Map<String,List<EnumMetadata>> readBundled(){try(InputStream raw=ReflectionMetadataRepository.class.getResourceAsStream("/codexus/heypixel/plugin/metadata/enum-struct.json.gz")){if(raw==null)throw new IllegalStateException("Bundled Heypixel mappings are missing");try(var input=new GZIPInputStream(raw)){return JSON.readValue(input,TYPE);}}catch(IOException e){throw new UncheckedIOException("Cannot read bundled Heypixel mappings",e);}}
    private static Map<String,List<EnumMetadata>> readDirectory(Path directory){Map<String,List<EnumMetadata>> result=new LinkedHashMap<>();if(!Files.isDirectory(directory))return result;try(var files=Files.walk(directory)){for(Path p:files.filter(f->Files.isRegularFile(f)&&f.getFileName().toString().endsWith(".json")).sorted().toList()){try(var input=Files.newInputStream(p)){Map<String,List<EnumMetadata>> parsed=JSON.readValue(input,TYPE);if(parsed!=null)result.putAll(parsed);}catch(IOException e){LOG.log(System.Logger.Level.WARNING,"Failed to load mapping file "+p,e);}}}catch(IOException e){throw new UncheckedIOException(e);}return result;}
    public static int classCount(){return mappings.size();}
    public static EnumFieldMetadata tryGetField(String className,String enumName,String fieldName){List<EnumMetadata> enums=mappings.get(className);if(enums==null)return null;for(EnumMetadata value:enums)if(Objects.equals(value.EnumName,enumName))return value.Fields==null?null:value.Fields.get(fieldName);return null;}
    public static String normalizeName(String value){if(value==null||value.isEmpty())return value;StringBuilder result=new StringBuilder(),run=new StringBuilder();for(char c:value.toCharArray()){if(c>=' '&&c<='~'){appendHashedRun(result,run);result.append(c);}else run.append(c);}appendHashedRun(result,run);return result.toString();}
    private static void appendHashedRun(StringBuilder result,StringBuilder run){if(run.isEmpty())return;result.append(HashExtensions.sha256Hex(run+"netease"));run.setLength(0);}
}
