/*
 * Java adaptation of SharpZipLib 1.4.2's deflater.
 * Copyright (c) 2000-2022 SharpZipLib Contributors.
 * Licensed under the MIT License; see META-INF/LICENSE-sharpziplib.txt.
 * Retains the original match search, Huffman tie-breaking and block decisions.
 */
package codexus.csharpsup.deflate;

public enum DeflateStrategy { Default, Filtered, HuffmanOnly }
