package codexus.cipher.captcha;

import ai.onnxruntime.*;
import codexus.development.sdk.captcha.CaptchaRecognizer;
import codexus.csharpsup.DotNetJson;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** In-process port of Codexus_Server CaptchaRecognitionService, using the same model and charset. */
public final class LocalCaptchaRecognizer implements CaptchaRecognizer {
    private static final int TARGET_WIDTH = 160;
    private static final int TARGET_HEIGHT = 64;
    private static final int THRESHOLD_BLOCK_SIZE = 11;
    private static final int THRESHOLD_C = 2;
    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final double[] GAUSSIAN_KERNEL = gaussianKernel(THRESHOLD_BLOCK_SIZE);
    private static final double GAUSSIAN_KERNEL_SUM = java.util.Arrays.stream(GAUSSIAN_KERNEL).sum();
    private final Path modelDirectory;
    private final int maxImageBytes;
    private final long maxImagePixels;
    private final OrtEnvironment environment;
    private OrtSession session;
    private String inputName;
    private List<String> charset = List.of();
    private boolean closed;

    /** Uses the model and charset packaged in the cipher JAR. */
    public LocalCaptchaRecognizer() {
        this(null, 1_048_576, 262_144);
    }
    public LocalCaptchaRecognizer(Path modelDirectory, int maxImageBytes, long maxImagePixels) {
        this.modelDirectory = modelDirectory;
        this.maxImageBytes = Math.max(1, maxImageBytes);
        this.maxImagePixels = Math.max(1, maxImagePixels);
        environment = OrtEnvironment.getEnvironment();
    }

    private void initialize() {
        if (closed) throw new IllegalStateException("Captcha recognizer is closed");
        if (session != null) return;
        try {
            charset = List.copyOf(DotNetJson.deserialize(new String(readModelFile("charset.json"), StandardCharsets.UTF_8),
                    new TypeReference<List<String>>() { }));
            try (var options = new OrtSession.SessionOptions()) {
                session = environment.createSession(readModelFile("common.onnx"), options);
            }
            validateModel();
        } catch (IOException | OrtException e) {
            closeFailedSession(e);
            throw new IllegalStateException("Unable to load the local captcha model", e);
        } catch (RuntimeException e) { closeFailedSession(e); throw e; }
    }
    private byte[] readModelFile(String name) throws IOException {
        if (modelDirectory != null) return Files.readAllBytes(modelDirectory.resolve(name));
        try (var stream = LocalCaptchaRecognizer.class.getResourceAsStream("/codexus/cipher/captcha/" + name)) {
            if (stream == null) throw new IOException("Missing packaged captcha model file: " + name);
            return stream.readAllBytes();
        }
    }
    private void closeFailedSession(Exception failure) {
        if (session == null) return;
        try { session.close(); } catch (OrtException closeFailure) { failure.addSuppressed(closeFailure); }
        finally { session = null; }
    }

    public synchronized String recognize(String base64Image) { return recognize(decodeBase64(base64Image)); }
    @Override public synchronized String recognize(byte[] imageBytes) {
        if (closed) throw new IllegalStateException("Captcha recognizer is closed");
        if (imageBytes == null || imageBytes.length == 0) throw badRequest("Captcha image is required");
        if (imageBytes.length > maxImageBytes) throw badRequest("Captcha image is too large");
        BufferedImage image = decodeImage(imageBytes);
        float[] input = preprocessImage(image);
        initialize();
        long[] shape = {1, 1, TARGET_HEIGHT, TARGET_WIDTH};
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), shape);
             OrtSession.Result results = session.run(Map.of(inputName, tensor))) {
            if (!(results.get(0) instanceof OnnxTensor output))
                throw new IllegalStateException("OCR model returned a non-tensor value");
            return decodeOutput(output);
        } catch (OrtException e) { throw new IllegalStateException("Local captcha inference failed", e); }
    }

    private void validateModel() throws OrtException {
        if (charset.isEmpty() || !charset.getFirst().isEmpty()) {
            throw new IllegalStateException("OCR charset must start with the CTC blank symbol");
        }
        inputName = session.getInputNames().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("OCR model has no input"));
        NodeInfo inputNode = session.getInputInfo().get(inputName);
        if (inputNode == null || !(inputNode.getInfo() instanceof TensorInfo inputInfo)
                || inputInfo.type != OnnxJavaType.FLOAT
                || !matches(inputInfo.getShape(), -1, 1, TARGET_HEIGHT, TARGET_WIDTH)) {
            throw new IllegalStateException("OCR model input must be float32 [batch,1,64,160]");
        }
        NodeInfo outputNode = session.getOutputInfo().values().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("OCR model has no output"));
        if (!(outputNode.getInfo() instanceof TensorInfo outputInfo)
                || outputInfo.type != OnnxJavaType.FLOAT) {
            throw new IllegalStateException("OCR model output must be float32");
        }
        long[] outputShape = outputInfo.getShape();
        if (outputShape.length != 3 || outputShape[2] != charset.size()) {
            throw new IllegalStateException("OCR model output classes do not match charset.json");
        }
    }

    private byte[] decodeBase64(String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            throw badRequest("Captcha image is required");
        }
        String value = base64Image.trim();
        if (value.regionMatches(true, 0, "data:image/", 0, "data:image/".length())) {
            int separator = value.indexOf(',');
            String metadata = separator < 0 ? value : value.substring(0, separator).toLowerCase(Locale.ROOT);
            if (separator < 0 || !metadata.endsWith(";base64")) {
                throw badRequest("Captcha image data URL must use Base64 encoding");
            }
            value = value.substring(separator + 1);
        }
        value = value.replace("\r", "").replace("\n", "").trim();
        long maxEncodedLength = ((long) maxImageBytes + 2) / 3 * 4;
        if (value.length() > maxEncodedLength) {
            throw new IllegalArgumentException("Captcha image is too large");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            if (decoded.length > maxImageBytes) {
                throw new IllegalArgumentException("Captcha image is too large");
            }
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw badRequest("Captcha image must be valid Base64");
        }
    }

    private BufferedImage decodeImage(byte[] imageBytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            if (input == null) {
                throw badRequest("Captcha body is not a supported image");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw badRequest("Captcha body is not a supported image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixels = Math.multiplyExact((long) width, height);
                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
                        || pixels > maxImagePixels) {
                    throw new IllegalArgumentException("Captcha image dimensions are too large");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (IOException | ArithmeticException exception) {
            throw badRequest("Captcha body is not a valid image");
        }
    }

    private static float[] preprocessImage(BufferedImage image) {
        BufferedImage grayImage = convertToGrayscale(image);
        BufferedImage thresholdedImage = adaptiveGaussianThreshold(grayImage);
        BufferedImage resizedImage = resizeImage(thresholdedImage);
        float[] result = new float[TARGET_WIDTH * TARGET_HEIGHT];
        int index = 0;
        for (int y = 0; y < TARGET_HEIGHT; y++) {
            for (int x = 0; x < TARGET_WIDTH; x++) {
                result[index++] = resizedImage.getRaster().getSample(x, y, 0) / 255.0f;
            }
        }
        return result;
    }

    private static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        var graphics = result.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private static BufferedImage adaptiveGaussianThreshold(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        var source = image.getRaster();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        var destination = result.getRaster();
        int halfBlock = THRESHOLD_BLOCK_SIZE / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double weightedSum = 0;
                int kernelIndex = 0;
                for (int dy = -halfBlock; dy <= halfBlock; dy++) {
                    for (int dx = -halfBlock; dx <= halfBlock; dx++) {
                        int sampleX = Math.clamp(x + dx, 0, width - 1);
                        int sampleY = Math.clamp(y + dy, 0, height - 1);
                        weightedSum += source.getSample(sampleX, sampleY, 0) * GAUSSIAN_KERNEL[kernelIndex++];
                    }
                }
                double threshold = weightedSum / GAUSSIAN_KERNEL_SUM - THRESHOLD_C;
                destination.setSample(x, y, 0, source.getSample(x, y, 0) > threshold ? 255 : 0);
            }
        }
        return result;
    }

    private static double[] gaussianKernel(int size) {
        double sigma = 0.3 * ((size - 1) * 0.5 - 1.0) + 0.8;
        int half = size / 2;
        double[] kernel = new double[size * size];
        int index = 0;
        for (int y = -half; y <= half; y++) {
            for (int x = -half; x <= half; x++) {
                kernel[index++] = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
            }
        }
        return kernel;
    }

    private static BufferedImage resizeImage(BufferedImage image) {
        BufferedImage result = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        var graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(image, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private String decodeOutput(OnnxTensor tensor) {
        long[] shape = tensor.getInfo().getShape();
        if (shape.length != 3 || shape[1] != 1 || shape[2] != charset.size()) {
            throw new IllegalStateException("OCR model returned an unexpected tensor shape");
        }
        int sequenceLength = Math.toIntExact(shape[0]);
        int classCount = Math.toIntExact(shape[2]);
        FloatBuffer values = tensor.getFloatBuffer();
        StringBuilder result = new StringBuilder();
        int lastIndex = -1;
        for (int position = 0; position < sequenceLength; position++) {
            int maxIndex = 0;
            float maxValue = Float.NEGATIVE_INFINITY;
            for (int label = 0; label < classCount; label++) {
                float value = values.get(position * classCount + label);
                if (value > maxValue) {
                    maxValue = value;
                    maxIndex = label;
                }
            }
            if (maxIndex != 0 && maxIndex != lastIndex) {
                result.append(charset.get(maxIndex));
            }
            lastIndex = maxIndex;
        }
        return result.toString();
    }

    private static boolean matches(long[] actual, long... expected) {
        if (actual.length != expected.length) {
            return false;
        }
        for (int i = 0; i < actual.length; i++) {
            if (expected[i] != -1 && actual[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static IllegalArgumentException badRequest(String message) {
        return new IllegalArgumentException(message);
    }
    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        if (session == null) return;
        try { session.close(); }
        catch (OrtException e) { throw new IllegalStateException("Unable to close the captcha model", e); }
        finally { session = null; }
    }
}
