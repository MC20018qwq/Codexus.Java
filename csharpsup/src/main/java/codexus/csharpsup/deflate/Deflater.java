/*
 * Java adaptation of SharpZipLib 1.4.2's deflater.
 * Copyright (c) 2000-2022 SharpZipLib Contributors.
 * Licensed under the MIT License; see META-INF/LICENSE-sharpziplib.txt.
 * Retains the original match search, Huffman tie-breaking and block decisions.
 */
package codexus.csharpsup.deflate;

public class Deflater
{
    public static final int BEST_COMPRESSION = 9;
    public static final int BEST_SPEED = 1;
    public static final int DEFAULT_COMPRESSION = -1;
    public static final int NO_COMPRESSION = 0;
    public static final int DEFLATED = 8;
    
    private static final int IS_SETDICT = 0x01;
    private static final int IS_FLUSHING = 0x04;
    private static final int IS_FINISHING = 0x08;
    private static final int INIT_STATE = 0x00;
    private static final int SETDICT_STATE = 0x01;
    private static final int BUSY_STATE = 0x10;
    private static final int FLUSHING_STATE = 0x14;
    private static final int FINISHING_STATE = 0x1c;
    private static final int FINISHED_STATE = 0x1e;
    private static final int CLOSED_STATE = 0x7f;
    public Deflater() { this(DEFAULT_COMPRESSION, false);
    }
    public Deflater(int level) { this(level, false);
    }
    public Deflater(int level, boolean noZlibHeaderOrFooter)
    {
        if (level == DEFAULT_COMPRESSION)
        {
            level = 6;
        }
        else if (level < NO_COMPRESSION || level > BEST_COMPRESSION)
        {
            throw new IllegalArgumentException("level");
        }
        pending = new DeflaterPending();
        engine = new DeflaterEngine(pending, noZlibHeaderOrFooter);
        this.noZlibHeaderOrFooter = noZlibHeaderOrFooter;
        SetStrategy(DeflateStrategy.Default);
        SetLevel(level);
        Reset();
    }
    public void Reset()
    {
        state = (noZlibHeaderOrFooter ? BUSY_STATE : INIT_STATE);
        totalOut = 0;
        pending.Reset();
        engine.Reset();
    }
    public int getAdler() {
            return engine.getAdler();
        }
    public long getTotalIn() {
            return engine.getTotalIn();
        }
    public long getTotalOut() {
            return totalOut;
        }
    public void Flush()
    {
        state |= IS_FLUSHING;
    }
    public void Finish()
    {
        state |= (IS_FLUSHING | IS_FINISHING);
    }
    public boolean getIsFinished() {
            return (state == FINISHED_STATE) && pending.getIsFlushed();
        }
    public boolean getIsNeedingInput() {
            return engine.NeedsInput();
        }
    public void SetInput(byte[] input)
    {
        SetInput(input, 0, input.length);
    }
    public void SetInput(byte[] input, int offset, int count)
    {
        if ((state & IS_FINISHING) != 0)
        {
            throw new IllegalStateException("Finish() already called");
        }
        engine.SetInput(input, offset, count);
    }
    public void SetLevel(int level)
    {
        if (level == DEFAULT_COMPRESSION)
        {
            level = 6;
        }
        else if (level < NO_COMPRESSION || level > BEST_COMPRESSION)
        {
            throw new IllegalArgumentException("level");
        }
        if (this.level != level)
        {
            this.level = level;
            engine.SetLevel(level);
        }
    }
    public int GetLevel()
    {
        return level;
    }
    public void SetStrategy(DeflateStrategy strategy)
    {
        engine.setStrategy(strategy);
    }
    public int Deflate(byte[] output)
    {
        return Deflate(output, 0, output.length);
    }
    public int Deflate(byte[] output, int offset, int length)
    {
        int origLength = length;
        if (state == CLOSED_STATE)
        {
            throw new IllegalStateException("Deflater closed");
        }
        if (state < BUSY_STATE)
        {
            int header = (DEFLATED +
                ((DeflaterConstants.MAX_WBITS - 8) << 4)) << 8;
            int level_flags = (level - 1) >> 1;
            if (level_flags < 0 || level_flags > 3)
            {
                level_flags = 3;
            }
            header |= level_flags << 6;
            if ((state & IS_SETDICT) != 0)
            {
                header |= DeflaterConstants.PRESET_DICT;
            }
            header += 31 - (header % 31);
            pending.WriteShortMSB(header);
            if ((state & IS_SETDICT) != 0)
            {
                int chksum = engine.getAdler();
                engine.ResetAdler();
                pending.WriteShortMSB(chksum >> 16);
                pending.WriteShortMSB(chksum & 0xffff);
            }
            state = BUSY_STATE | (state & (IS_FLUSHING | IS_FINISHING));
        }
        for (; ; )
        {
            int count = pending.Flush(output, offset, length);
            offset += count;
            totalOut += count;
            length -= count;
            if (length == 0 || state == FINISHED_STATE)
            {
                break;
            }
            if (!engine.Deflate((state & IS_FLUSHING) != 0, (state & IS_FINISHING) != 0))
            {
                switch (state)
                {
                    case BUSY_STATE:
                        return origLength - length;
                    case FLUSHING_STATE:
                        if (level != NO_COMPRESSION)
                        {
                            int neededbits = 8 + ((-pending.getBitCount()) & 7);
                            while (neededbits > 0)
                            {
                                pending.WriteBits(2, 10);
                                neededbits -= 10;
                            }
                        }
                        state = BUSY_STATE;
                        break;
                    case FINISHING_STATE:
                        pending.AlignToByte();
                        if (!noZlibHeaderOrFooter)
                        {
                            int adler = engine.getAdler();
                            pending.WriteShortMSB(adler >> 16);
                            pending.WriteShortMSB(adler & 0xffff);
                        }
                        state = FINISHED_STATE;
                        break;
                }
            }
        }
        return origLength - length;
    }
    public void SetDictionary(byte[] dictionary)
    {
        SetDictionary(dictionary, 0, dictionary.length);
    }
    public void SetDictionary(byte[] dictionary, int index, int count)
    {
        if (state != INIT_STATE)
        {
            throw new IllegalStateException();
        }
        state = SETDICT_STATE;
        engine.SetDictionary(dictionary, index, count);
    }
    private int level;
    private boolean noZlibHeaderOrFooter;
    private int state;
    private long totalOut;
    private DeflaterPending pending;
    private DeflaterEngine engine;
}
