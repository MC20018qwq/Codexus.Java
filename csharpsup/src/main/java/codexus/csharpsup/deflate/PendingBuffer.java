/*
 * Java adaptation of SharpZipLib 1.4.2's deflater.
 * Copyright (c) 2000-2022 SharpZipLib Contributors.
 * Licensed under the MIT License; see META-INF/LICENSE-sharpziplib.txt.
 * Retains the original match search, Huffman tie-breaking and block decisions.
 */
package codexus.csharpsup.deflate;

public class PendingBuffer
{
    private final byte[] buffer;
    private int start;
    private int end;
    private int bits;
    private int bitCount;
    public PendingBuffer() { this(4096);
    }
    public PendingBuffer(int bufferSize)
    {
        buffer = new byte[bufferSize];
    }
    public void Reset()
    {
        start = end = bitCount = 0;
    }
    public void WriteByte(int value)
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        buffer[end++] = (byte)value;
    }
    public void WriteShort(int value)
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        buffer[end++] = (byte)value;
        buffer[end++] = (byte)(value >> 8);
    }
    public void WriteInt(int value)
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        buffer[end++] = (byte)value;
        buffer[end++] = (byte)(value >> 8);
        buffer[end++] = (byte)(value >> 16);
        buffer[end++] = (byte)(value >> 24);
    }
    public void WriteBlock(byte[] block, int offset, int length)
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        System.arraycopy(block, offset, buffer, end, length);
        end += length;
    }
    public int getBitCount() {
            return bitCount;
        }
    public void AlignToByte()
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        if (bitCount > 0)
        {
            buffer[end++] = (byte)bits;
            if (bitCount > 8)
            {
                buffer[end++] = (byte)(bits >> 8);
            }
        }
        bits = 0;
        bitCount = 0;
    }
    public void WriteBits(int b, int count)
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        bits |= (int)(b << bitCount);
        bitCount += count;
        if (bitCount >= 16)
        {
            buffer[end++] = (byte)bits;
            buffer[end++] = (byte)(bits >> 8);
            bits >>>= 16;
            bitCount -= 16;
        }
    }
    public void WriteShortMSB(int s)
    {
        if (DeflaterConstants.DEBUGGING && (start != 0) )
        {
            throw new IllegalStateException("Debug check: start != 0");
        }
        buffer[end++] = (byte)(s >> 8);
        buffer[end++] = (byte)s;
    }
    public boolean getIsFlushed() {
            return end == 0;
        }
    public int Flush(byte[] output, int offset, int length)
    {
        if (bitCount >= 8)
        {
            buffer[end++] = (byte)bits;
            bits >>>= 8;
            bitCount -= 8;
        }
        if (length > end - start)
        {
            length = end - start;
            System.arraycopy(buffer, start, output, offset, length);
            start = 0;
            end = 0;
        }
        else
        {
            System.arraycopy(buffer, start, output, offset, length);
            start += length;
        }
        return length;
    }
    public byte[] ToByteArray()
    {
        AlignToByte();
        byte[] result = new byte[end - start];
        System.arraycopy(buffer, start, result, 0, result.length);
        start = 0;
        end = 0;
        return result;
    }
}
