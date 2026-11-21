package codexus.gateway.message;

import codexus.gateway.connection.ConnectionState;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterMessage {
    ConnectionState state();
    String type();
}
