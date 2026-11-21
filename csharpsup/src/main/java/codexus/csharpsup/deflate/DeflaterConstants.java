/*
 * Java adaptation of SharpZipLib 1.4.2's deflater.
 * Copyright (c) 2000-2022 SharpZipLib Contributors.
 * Licensed under the MIT License; see META-INF/LICENSE-sharpziplib.txt.
 * Retains the original match search, Huffman tie-breaking and block decisions.
 */
package codexus.csharpsup.deflate;

public final class DeflaterConstants
{
    public static final boolean DEBUGGING = false;
    public static final int STORED_BLOCK = 0;
    public static final int STATIC_TREES = 1;
    public static final int DYN_TREES = 2;
    public static final int PRESET_DICT = 0x20;
    public static final int DEFAULT_MEM_LEVEL = 8;
    public static final int MAX_MATCH = 258;
    public static final int MIN_MATCH = 3;
    public static final int MAX_WBITS = 15;
    public static final int WSIZE = 1 << MAX_WBITS;
    public static final int WMASK = WSIZE - 1;
    public static final int HASH_BITS = DEFAULT_MEM_LEVEL + 7;
    public static final int HASH_SIZE = 1 << HASH_BITS;
    public static final int HASH_MASK = HASH_SIZE - 1;
    public static final int HASH_SHIFT = (HASH_BITS + MIN_MATCH - 1) / MIN_MATCH;
    public static final int MIN_LOOKAHEAD = MAX_MATCH + MIN_MATCH + 1;
    public static final int MAX_DIST = WSIZE - MIN_LOOKAHEAD;
    public static final int PENDING_BUF_SIZE = 1 << (DEFAULT_MEM_LEVEL + 8);
    public static int MAX_BLOCK_SIZE = Math.min(65535, PENDING_BUF_SIZE - 5);
    public static final int DEFLATE_STORED = 0;
    public static final int DEFLATE_FAST = 1;
    public static final int DEFLATE_SLOW = 2;
    public static int[] GOOD_LENGTH = { 0, 4, 4, 4, 4, 8, 8, 8, 32, 32 };
    public static int[] MAX_LAZY = { 0, 4, 5, 6, 4, 16, 16, 32, 128, 258 };
    public static int[] NICE_LENGTH = { 0, 8, 16, 32, 16, 32, 128, 128, 258, 258 };
    public static int[] MAX_CHAIN = { 0, 4, 8, 32, 16, 32, 128, 256, 1024, 4096 };
    public static int[] COMPR_FUNC = { 0, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
}
