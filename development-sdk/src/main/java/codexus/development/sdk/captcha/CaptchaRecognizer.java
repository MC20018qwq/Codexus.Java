package codexus.development.sdk.captcha;

/** A local captcha model supplied by the cipher module. */
public interface CaptchaRecognizer extends AutoCloseable {
    String recognize(byte[] image);
    @Override void close();
}
