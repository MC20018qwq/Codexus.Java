package codexus.development.sdk.utils;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.net.*;
import java.io.*;
public final class ClassCatalog {
    private ClassCatalog() {}
    public static List<Class<?>> classPathClasses(){
        List<Class<?>> result=new ArrayList<>();
        for(String element:System.getProperty("java.class.path").split(java.io.File.pathSeparator))result.addAll(classes(Path.of(element),Thread.currentThread().getContextClassLoader()));
        return result;
    }
    public static List<Class<?>> classes(Path source,ClassLoader loader){
        List<String> names=new ArrayList<>();List<Class<?>> result=new ArrayList<>();
        try {
            if(Files.isDirectory(source)){try(var paths=Files.walk(source)){paths.filter(p->p.toString().endsWith(".class")).forEach(p->names.add(source.relativize(p).toString().replace('\\','.').replace('/','.')));}}
            else if((source.toString().endsWith(".jar") || source.toString().endsWith(".ug"))){try(JarFile jar=new JarFile(source.toFile())){jar.stream().map(JarEntry::getName).filter(n->n.endsWith(".class")&&!n.startsWith("META-INF/")).forEach(n->names.add(n.replace('/','.')));}}
            for(String name:names){name=name.substring(0,name.length()-6);if(name.endsWith("module-info")||name.endsWith("package-info"))continue;try{result.add(Class.forName(name,false,loader));}catch(LinkageError|ClassNotFoundException ignored){}}
        }catch(IOException e){throw new UncheckedIOException(e);}return result;
    }
}
