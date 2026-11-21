package codexus.cipher.utils.exception;
public class CaptchaException extends RuntimeException {
    public CaptchaException(String message) { super(message); }
    public static CaptchaException clone(CaptchaException exception, String append) {
        return new CaptchaException(exception.getMessage() + "|" + (append == null ? "" : append));
    }
}
