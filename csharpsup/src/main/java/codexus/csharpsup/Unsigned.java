package codexus.csharpsup;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
public @interface Unsigned { int value(); }
