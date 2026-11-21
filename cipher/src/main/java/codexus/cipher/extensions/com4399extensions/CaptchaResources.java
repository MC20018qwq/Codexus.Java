package codexus.cipher.extensions.com4399extensions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class CaptchaResources {
    private CaptchaResources() { }
    public static final String JigsawCaptchaHtml = load("jigsaw.html");
    public static final String ClickCaptchaHtml = load("click.html");

    private static String load(String name) {
        try (var input = CaptchaResources.class.getResourceAsStream("/codexus/cipher/com4399/" + name)) {
            if (input == null) throw new IllegalStateException("Missing captcha page: " + name);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
