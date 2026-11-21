package codexus.development.sdk.attributes;
import java.lang.annotation.*;
import codexus.development.sdk.enums.EnumLoaderType;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentLoader { EnumLoaderType value(); }
