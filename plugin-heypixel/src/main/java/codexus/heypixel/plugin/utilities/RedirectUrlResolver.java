package codexus.heypixel.plugin.utilities;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
public final class RedirectUrlResolver {
    private RedirectUrlResolver(){}
    public static CompletableFuture<String> resolveAsync(String url){return CompletableFuture.supplyAsync(()->resolve(url));}
    private static String resolve(String url){try(var client=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(30)).build()){URI current=URI.create(url);for(int i=0;i<10;i++){var response=client.send(HttpRequest.newBuilder(current).timeout(Duration.ofSeconds(30)).GET().build(),HttpResponse.BodyHandlers.discarding());int s=response.statusCode();var location=response.headers().firstValue("Location");if(!((s==301)||(s==302)||(s==303)||(s==307)||(s==308))||location.isEmpty())return current.toString();current=current.resolve(location.get());}return current.toString();}catch(Exception e){if(e instanceof InterruptedException)Thread.currentThread().interrupt();return url;}}
}
