package codexus.cipher.connection.chacha;

import java.nio.*;

public final class ChaChaOfSalsa {
    private final int[] state = new int[16];
    private final byte[] keyStream = new byte[64];
    private final int rounds;
    private int position;
    private boolean exhausted;
    public ChaChaOfSalsa(byte[] key, byte[] iv, boolean encryption) { this(key, iv, encryption, 8); }
    public ChaChaOfSalsa(byte[] key, byte[] iv, boolean encryption, int rounds) {
        if (key.length != 32) throw new IllegalArgumentException("ChaCha7539 requires a 256-bit key");
        if (iv.length != 12) throw new IllegalArgumentException("ChaCha7539 requires exactly 12 bytes of IV");
        this.rounds = rounds;
        state[0] = 0x61707865; state[1] = 0x3320646e; state[2] = 0x79622d32; state[3] = 0x6b206574;
        ByteBuffer keyWords = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 8; i++) state[4 + i] = keyWords.getInt();
        ByteBuffer nonce = ByteBuffer.wrap(iv).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 3; i++) state[13 + i] = nonce.getInt();
    }
    public String getAlgorithmName() { return "ChaCha" + rounds; }
    public int processBytes(byte[] input, int inputOffset, int length, byte[] output, int outputOffset) {
        java.util.Objects.checkFromIndexSize(inputOffset, length, input.length);
        java.util.Objects.checkFromIndexSize(outputOffset, length, output.length);
        for (int i = 0; i < length; i++) {
            if (position == 0) {
                if (exhausted) throw new IllegalStateException("attempt to increase counter past 2^32");
                generateKeyStream();
            }
            output[outputOffset + i] = (byte) (input[inputOffset + i] ^ keyStream[position]);
            position = (position + 1) & 63;
            if (position == 0 && ++state[12] == 0) exhausted = true;
        }
        return length;
    }
    private void generateKeyStream() {
        if ((rounds & 1) != 0) throw new IllegalArgumentException("Number of rounds must be even");
        int[] x = state.clone();
        for (int i = rounds; i > 0; i -= 2) {
            quarter(x, 0, 4, 8, 12); quarter(x, 1, 5, 9, 13); quarter(x, 2, 6, 10, 14); quarter(x, 3, 7, 11, 15);
            quarter(x, 0, 5, 10, 15); quarter(x, 1, 6, 11, 12); quarter(x, 2, 7, 8, 13); quarter(x, 3, 4, 9, 14);
        }
        ByteBuffer buffer = ByteBuffer.wrap(keyStream).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 16; i++) buffer.putInt(x[i] + state[i]);
    }
    private static void quarter(int[] x, int a, int b, int c, int d) {
        x[a] += x[b]; x[d] = Integer.rotateLeft(x[d] ^ x[a], 16);
        x[c] += x[d]; x[b] = Integer.rotateLeft(x[b] ^ x[c], 12);
        x[a] += x[b]; x[d] = Integer.rotateLeft(x[d] ^ x[a], 8);
        x[c] += x[d]; x[b] = Integer.rotateLeft(x[b] ^ x[c], 7);
    }
}
