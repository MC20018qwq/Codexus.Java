package codexus.development.sdk.attributes;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Plugin {
    String id();
    String name();
    String description();
    String author();
    String version();
    String[] dependencies() default {};
}
