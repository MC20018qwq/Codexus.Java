package codexus.csharpsup;
public class HttpRequestException extends RuntimeException {
    private final Integer statusCode;
    public HttpRequestException(String message) { super(message); statusCode = null; }
    public HttpRequestException(String message, Throwable cause) { super(message, cause); statusCode = null; }
    public HttpRequestException(String message, int statusCode) { super(message); this.statusCode = statusCode; }
    public Integer getStatusCode() { return statusCode; }
}
