package codexus.gateway.extensions;

import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

public final class ByteArrayExtensions {
    private ByteArrayExtensions() { }
    public static ECPublicKey toEcDiffieHellmanPublicKey(byte[] encoded) {
        try {
            var key = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encoded));
            var parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(EcCurveExtensions.DefaultCurve);
            var expected = parameters.getParameterSpec(ECParameterSpec.class);
            if (!key.getParams().getCurve().equals(expected.getCurve()) || !key.getParams().getGenerator().equals(expected.getGenerator())
                    || !key.getParams().getOrder().equals(expected.getOrder()) || key.getParams().getCofactor() != expected.getCofactor())
                throw new IllegalArgumentException("Gateway handshake requires NIST P-256");
            return key;
        } catch (GeneralSecurityException e) { throw new IllegalArgumentException("Invalid ECDH public key", e); }
    }
}
