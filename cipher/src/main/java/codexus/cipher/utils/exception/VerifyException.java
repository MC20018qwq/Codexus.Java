package codexus.cipher.utils.exception;
public class VerifyException extends RuntimeException {
    public VerifyException(String message) { super(message); }
    public static VerifyException clone(VerifyException exception, String append) {
        return new VerifyException(exception.getMessage() + "|" + (append == null ? "" : append));
    }
}
