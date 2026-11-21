/*
 * Java adaptation of SharpZipLib 1.4.2's deflater.
 * Copyright (c) 2000-2022 SharpZipLib Contributors.
 * Licensed under the MIT License; see META-INF/LICENSE-sharpziplib.txt.
 * Retains the original match search, Huffman tie-breaking and block decisions.
 */
package codexus.csharpsup.deflate;

import java.util.zip.Adler32;

public class DeflaterEngine
{
    private static final int TooFar = 4096;
    public DeflaterEngine(DeflaterPending pending) { this(pending, false);
    }
    public DeflaterEngine(DeflaterPending pending, boolean noAdlerCalculation)
    {
        this.pending = pending;
        huffman = new DeflaterHuffman(pending);
        if (!noAdlerCalculation)
            adler = new Adler32();
        window = new byte[2 * DeflaterConstants.WSIZE];
        head = new short[DeflaterConstants.HASH_SIZE];
        prev = new short[DeflaterConstants.WSIZE];
        blockStart = strstart = 1;
    }
    public boolean Deflate(boolean flush, boolean finish)
    {
        boolean progress;
        do
        {
            FillWindow();
            boolean canFlush = flush && (inputOff == inputEnd);
            if (DeflaterConstants.DEBUGGING) {
                System.out.println("window: [" + blockStart + "," + strstart + ","
                            + lookahead + "], " + compressionFunction + "," + canFlush);
            }
            switch (compressionFunction)
            {
                case DeflaterConstants.DEFLATE_STORED:
                    progress = DeflateStored(canFlush, finish);
                    break;
                case DeflaterConstants.DEFLATE_FAST:
                    progress = DeflateFast(canFlush, finish);
                    break;
                case DeflaterConstants.DEFLATE_SLOW:
                    progress = DeflateSlow(canFlush, finish);
                    break;
                default:
                    throw new IllegalStateException("unknown compressionFunction");
            }
        } while (pending.getIsFlushed() && progress); 
        return progress;
    }
    public void SetInput(byte[] buffer, int offset, int count)
    {
        if (buffer == null)
        {
            throw new IllegalArgumentException("buffer");
        }
        if (offset < 0)
        {
            throw new IllegalArgumentException("offset");
        }
        if (count < 0)
        {
            throw new IllegalArgumentException("count");
        }
        if (inputOff < inputEnd)
        {
            throw new IllegalStateException("Old input was not completely processed");
        }
        int end = offset + count;
        if ((offset > end) || (end > buffer.length))
        {
            throw new IllegalArgumentException("count");
        }
        inputBuf = buffer;
        inputOff = offset;
        inputEnd = end;
    }
    public boolean NeedsInput()
    {
        return (inputEnd == inputOff);
    }
    public void SetDictionary(byte[] buffer, int offset, int length)
    {
        if (DeflaterConstants.DEBUGGING && (strstart != 1) )
        {
            throw new IllegalStateException("strstart not 1");
        }
        if (adler != null) adler.update(buffer, offset, length);
        if (length < DeflaterConstants.MIN_MATCH)
        {
            return;
        }
        if (length > DeflaterConstants.MAX_DIST)
        {
            offset += length - DeflaterConstants.MAX_DIST;
            length = DeflaterConstants.MAX_DIST;
        }
        System.arraycopy(buffer, offset, window, strstart, length);
        UpdateHash();
        --length;
        while (--length > 0)
        {
            InsertString();
            strstart++;
        }
        strstart += 2;
        blockStart = strstart;
    }
    public void Reset()
    {
        huffman.Reset();
        if (adler != null) adler.reset();
        blockStart = strstart = 1;
        lookahead = 0;
        totalIn = 0;
        prevAvailable = false;
        matchLen = DeflaterConstants.MIN_MATCH - 1;
        for (int i = 0; i < DeflaterConstants.HASH_SIZE; i++)
        {
            head[i] = 0;
        }
        for (int i = 0; i < DeflaterConstants.WSIZE; i++)
        {
            prev[i] = 0;
        }
    }
    public void ResetAdler()
    {
        if (adler != null) adler.reset();
    }
    public int getAdler() {
            return (adler != null) ? (int)adler.getValue() : 0;
        }
    public long getTotalIn() {
            return totalIn;
        }
    public DeflateStrategy getStrategy() {
            return strategy;
        }
public void setStrategy(DeflateStrategy value) {
            strategy = value;
        }
    public void SetLevel(int level)
    {
        if ((level < 0) || (level > 9))
        {
            throw new IllegalArgumentException("level");
        }
        goodLength = DeflaterConstants.GOOD_LENGTH[level];
        max_lazy = DeflaterConstants.MAX_LAZY[level];
        niceLength = DeflaterConstants.NICE_LENGTH[level];
        max_chain = DeflaterConstants.MAX_CHAIN[level];
        if (DeflaterConstants.COMPR_FUNC[level] != compressionFunction)
        {
            if (DeflaterConstants.DEBUGGING) {
               System.out.println("Change from " + compressionFunction + " to "
                                      + DeflaterConstants.COMPR_FUNC[level]);
            }
            switch (compressionFunction)
            {
                case DeflaterConstants.DEFLATE_STORED:
                    if (strstart > blockStart)
                    {
                        huffman.FlushStoredBlock(window, blockStart,
                            strstart - blockStart, false);
                        blockStart = strstart;
                    }
                    UpdateHash();
                    break;
                case DeflaterConstants.DEFLATE_FAST:
                    if (strstart > blockStart)
                    {
                        huffman.FlushBlock(window, blockStart, strstart - blockStart,
                            false);
                        blockStart = strstart;
                    }
                    break;
                case DeflaterConstants.DEFLATE_SLOW:
                    if (prevAvailable)
                    {
                        huffman.TallyLit(window[strstart - 1] & 0xff);
                    }
                    if (strstart > blockStart)
                    {
                        huffman.FlushBlock(window, blockStart, strstart - blockStart, false);
                        blockStart = strstart;
                    }
                    prevAvailable = false;
                    matchLen = DeflaterConstants.MIN_MATCH - 1;
                    break;
            }
            compressionFunction = DeflaterConstants.COMPR_FUNC[level];
        }
    }
    public void FillWindow()
    {
        if (strstart >= DeflaterConstants.WSIZE + DeflaterConstants.MAX_DIST)
        {
            SlideWindow();
        }
        if (lookahead < DeflaterConstants.MIN_LOOKAHEAD && inputOff < inputEnd)
        {
            int more = 2 * DeflaterConstants.WSIZE - lookahead - strstart;
            if (more > inputEnd - inputOff)
            {
                more = inputEnd - inputOff;
            }
            System.arraycopy(inputBuf, inputOff, window, strstart + lookahead, more);
            if (adler != null) adler.update(inputBuf, inputOff, more);
            inputOff += more;
            totalIn += more;
            lookahead += more;
        }
        if (lookahead >= DeflaterConstants.MIN_MATCH)
        {
            UpdateHash();
        }
    }
    private void UpdateHash()
    {
        ins_h = ((window[strstart] & 255) << DeflaterConstants.HASH_SHIFT) ^ (window[strstart + 1] & 255);
    }
    private int InsertString()
    {
        short match;
        int hash = ((ins_h << DeflaterConstants.HASH_SHIFT) ^ (window[strstart + (DeflaterConstants.MIN_MATCH - 1)] & 255)) & DeflaterConstants.HASH_MASK;
        if (DeflaterConstants.DEBUGGING)
        {
            if (hash != (((window[strstart] << (2*DeflaterConstants.HASH_SHIFT)) ^
                              (window[strstart + 1] << DeflaterConstants.HASH_SHIFT) ^
                              (window[strstart + 2])) & DeflaterConstants.HASH_MASK)) {
                    throw new IllegalStateException("hash inconsistent: " + hash + "/"
                                            +window[strstart] + ","
                                            +window[strstart + 1] + ","
                                            +window[strstart + 2] + "," + DeflaterConstants.HASH_SHIFT);
                }
        }
        prev[strstart & DeflaterConstants.WMASK] = match = head[hash];
        head[hash] = (short)strstart;
        ins_h = hash;
        return match & 0xffff;
    }
    private void SlideWindow()
    {
        System.arraycopy(window, DeflaterConstants.WSIZE, window, 0, DeflaterConstants.WSIZE);
        matchStart -= DeflaterConstants.WSIZE;
        strstart -= DeflaterConstants.WSIZE;
        blockStart -= DeflaterConstants.WSIZE;
        for (int i = 0; i < DeflaterConstants.HASH_SIZE; ++i)
        {
            int m = head[i] & 0xffff;
            head[i] = (short)(m >= DeflaterConstants.WSIZE ? (m - DeflaterConstants.WSIZE) : 0);
        }
        for (int i = 0; i < DeflaterConstants.WSIZE; i++)
        {
            int m = prev[i] & 0xffff;
            prev[i] = (short)(m >= DeflaterConstants.WSIZE ? (m - DeflaterConstants.WSIZE) : 0);
        }
    }
    private boolean FindLongestMatch(int curMatch)
    {
        int match;
        int scan = strstart;
        int scanMax = scan + Math.min(DeflaterConstants.MAX_MATCH, lookahead) - 1;
        int limit = Math.max(scan - DeflaterConstants.MAX_DIST, 0);
        byte[] window = this.window;
        short[] prev = this.prev;
        int chainLength = this.max_chain;
        int niceLength = Math.min(this.niceLength, lookahead);
        matchLen = Math.max(matchLen, DeflaterConstants.MIN_MATCH - 1);
        if (scan + matchLen > scanMax) return false;
        byte scan_end1 = window[scan + matchLen - 1];
        byte scan_end = window[scan + matchLen];
        if (matchLen >= this.goodLength) chainLength >>= 2;
        do
        {
            match = curMatch;
            scan = strstart;
            if (window[match + matchLen] != scan_end
             || window[match + matchLen - 1] != scan_end1
             || window[match] != window[scan]
             || window[++match] != window[++scan])
            {
                continue;
            }
            switch ((scanMax - scan) % 8)
            {
                case 1:
                    if (window[++scan] == window[++match]) break;
                    break;
                case 2:
                    if (window[++scan] == window[++match]
              && window[++scan] == window[++match]) break;
                    break;
                case 3:
                    if (window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]) break;
                    break;
                case 4:
                    if (window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]) break;
                    break;
                case 5:
                    if (window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]) break;
                    break;
                case 6:
                    if (window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]) break;
                    break;
                case 7:
                    if (window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]
              && window[++scan] == window[++match]) break;
                    break;
            }
            if (window[scan] == window[match])
            {
                do
                {
                    if (scan == scanMax)
                    {
                        ++scan;     
                        ++match;
                        break;
                    }
                }
                while (window[++scan] == window[++match]
                    && window[++scan] == window[++match]
                    && window[++scan] == window[++match]
                    && window[++scan] == window[++match]
                    && window[++scan] == window[++match]
                    && window[++scan] == window[++match]
                    && window[++scan] == window[++match]
                    && window[++scan] == window[++match]);
            }
            if (scan - strstart > matchLen)
            {
              if (DeflaterConstants.DEBUGGING && (ins_h == 0) )
              System.err.println("Found match: " + curMatch + "-" + (scan - strstart));
                matchStart = curMatch;
                matchLen = scan - strstart;
                if (matchLen >= niceLength)
                    break;
                scan_end1 = window[scan - 1];
                scan_end = window[scan];
            }
        } while ((curMatch = (prev[curMatch & DeflaterConstants.WMASK] & 0xffff)) > limit && 0 != --chainLength);
        return matchLen >= DeflaterConstants.MIN_MATCH;
    }
    private boolean DeflateStored(boolean flush, boolean finish)
    {
        if (!flush && (lookahead == 0))
        {
            return false;
        }
        strstart += lookahead;
        lookahead = 0;
        int storedLength = strstart - blockStart;
        if ((storedLength >= DeflaterConstants.MAX_BLOCK_SIZE) || 
            (blockStart < DeflaterConstants.WSIZE && storedLength >= DeflaterConstants.MAX_DIST) ||   
            flush)
        {
            boolean lastBlock = finish;
            if (storedLength > DeflaterConstants.MAX_BLOCK_SIZE)
            {
                storedLength = DeflaterConstants.MAX_BLOCK_SIZE;
                lastBlock = false;
            }
            if (DeflaterConstants.DEBUGGING)
            {
               System.out.println("storedBlock[" + storedLength + "," + lastBlock + "]");
            }
            huffman.FlushStoredBlock(window, blockStart, storedLength, lastBlock);
            blockStart += storedLength;
            return !(lastBlock || storedLength == 0);
        }
        return true;
    }
    private boolean DeflateFast(boolean flush, boolean finish)
    {
        if (lookahead < DeflaterConstants.MIN_LOOKAHEAD && !flush)
        {
            return false;
        }
        while (lookahead >= DeflaterConstants.MIN_LOOKAHEAD || flush)
        {
            if (lookahead == 0)
            {
                huffman.FlushBlock(window, blockStart, strstart - blockStart, finish);
                blockStart = strstart;
                return false;
            }
            if (strstart > 2 * DeflaterConstants.WSIZE - DeflaterConstants.MIN_LOOKAHEAD)
            {
                SlideWindow();
            }
            int hashHead;
            if (lookahead >= DeflaterConstants.MIN_MATCH &&
                (hashHead = InsertString()) != 0 &&
                strategy != DeflateStrategy.HuffmanOnly &&
                strstart - hashHead <= DeflaterConstants.MAX_DIST &&
                FindLongestMatch(hashHead))
            {
                if (DeflaterConstants.DEBUGGING)
                {
                    for (int i = 0 ; i < matchLen; i++) {
                        if (window[strstart + i] != window[matchStart + i]) {
                            throw new IllegalStateException("Match failure");
                        }
                    }
                }
                boolean full = huffman.TallyDist(strstart - matchStart, matchLen);
                lookahead -= matchLen;
                if (matchLen <= max_lazy && lookahead >= DeflaterConstants.MIN_MATCH)
                {
                    while (--matchLen > 0)
                    {
                        ++strstart;
                        InsertString();
                    }
                    ++strstart;
                }
                else
                {
                    strstart += matchLen;
                    if (lookahead >= DeflaterConstants.MIN_MATCH - 1)
                    {
                        UpdateHash();
                    }
                }
                matchLen = DeflaterConstants.MIN_MATCH - 1;
                if (!full)
                {
                    continue;
                }
            }
            else
            {
                huffman.TallyLit(window[strstart] & 0xff);
                ++strstart;
                --lookahead;
            }
            if (huffman.IsFull())
            {
                boolean lastBlock = finish && (lookahead == 0);
                huffman.FlushBlock(window, blockStart, strstart - blockStart, lastBlock);
                blockStart = strstart;
                return !lastBlock;
            }
        }
        return true;
    }
    private boolean DeflateSlow(boolean flush, boolean finish)
    {
        if (lookahead < DeflaterConstants.MIN_LOOKAHEAD && !flush)
        {
            return false;
        }
        while (lookahead >= DeflaterConstants.MIN_LOOKAHEAD || flush)
        {
            if (lookahead == 0)
            {
                if (prevAvailable)
                {
                    huffman.TallyLit(window[strstart - 1] & 0xff);
                }
                prevAvailable = false;
                if (DeflaterConstants.DEBUGGING && !flush)
                {
                    throw new IllegalStateException("Not flushing, but no lookahead");
                }
                huffman.FlushBlock(window, blockStart, strstart - blockStart,
                    finish);
                blockStart = strstart;
                return false;
            }
            if (strstart >= 2 * DeflaterConstants.WSIZE - DeflaterConstants.MIN_LOOKAHEAD)
            {
                SlideWindow();
            }
            int prevMatch = matchStart;
            int prevLen = matchLen;
            if (lookahead >= DeflaterConstants.MIN_MATCH)
            {
                int hashHead = InsertString();
                if (strategy != DeflateStrategy.HuffmanOnly &&
                    hashHead != 0 &&
                    strstart - hashHead <= DeflaterConstants.MAX_DIST &&
                    FindLongestMatch(hashHead))
                {
                    if (matchLen <= 5 && (strategy == DeflateStrategy.Filtered || (matchLen == DeflaterConstants.MIN_MATCH && strstart - matchStart > TooFar)))
                    {
                        matchLen = DeflaterConstants.MIN_MATCH - 1;
                    }
                }
            }
            if ((prevLen >= DeflaterConstants.MIN_MATCH) && (matchLen <= prevLen))
            {
                if (DeflaterConstants.DEBUGGING)
                {
                   for (int i = 0 ; i < matchLen; i++) {
                      if (window[strstart-1+i] != window[prevMatch + i])
                         throw new IllegalStateException();
                    }
                }
                huffman.TallyDist(strstart - 1 - prevMatch, prevLen);
                prevLen -= 2;
                do
                {
                    strstart++;
                    lookahead--;
                    if (lookahead >= DeflaterConstants.MIN_MATCH)
                    {
                        InsertString();
                    }
                } while (--prevLen > 0);
                strstart++;
                lookahead--;
                prevAvailable = false;
                matchLen = DeflaterConstants.MIN_MATCH - 1;
            }
            else
            {
                if (prevAvailable)
                {
                    huffman.TallyLit(window[strstart - 1] & 0xff);
                }
                prevAvailable = true;
                strstart++;
                lookahead--;
            }
            if (huffman.IsFull())
            {
                int len = strstart - blockStart;
                if (prevAvailable)
                {
                    len--;
                }
                boolean lastBlock = (finish && (lookahead == 0) && !prevAvailable);
                huffman.FlushBlock(window, blockStart, len, lastBlock);
                blockStart += len;
                return !lastBlock;
            }
        }
        return true;
    }
    private int ins_h;
    private short[] head;
    private short[] prev;
    private int matchStart;
    private int matchLen;
    private boolean prevAvailable;
    private int blockStart;
    private int strstart;
    private int lookahead;
    private byte[] window;
    private DeflateStrategy strategy;
    private int max_chain, max_lazy, niceLength, goodLength;
    private int compressionFunction;
    private byte[] inputBuf;
    private long totalIn;
    private int inputOff;
    private int inputEnd;
    private DeflaterPending pending;
    private DeflaterHuffman huffman;
    private Adler32 adler;
}
