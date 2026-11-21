package codexus.gateway.extensions;

import java.security.spec.ECGenParameterSpec;

public final class EcCurveExtensions {
    private EcCurveExtensions() { }
    public static final ECGenParameterSpec DefaultCurve = new ECGenParameterSpec("secp256r1");
}
