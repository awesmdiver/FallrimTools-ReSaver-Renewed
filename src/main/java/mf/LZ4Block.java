package mf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Pure-Java LZ4 block format compressor and decompressor.
 * Replaces the lz4-pure-java dependency to eliminate sun.misc.Unsafe usage.
 * Implements the LZ4 block format as documented at https://lz4.github.io/lz4/
 */
public final class LZ4Block {

    private static final int MIN_MATCH      = 4;
    private static final int LAST_LITERALS  = 5;
    private static final int HASH_LOG       = 16;
    private static final int HASH_SIZE      = 1 << HASH_LOG;

    /** Decompresses LZ4 block data. */
    public static ByteBuffer decompress(ByteBuffer compressed, int uncompressedSize) throws IOException {
        byte[] src = toArray(compressed);
        byte[] dst = new byte[uncompressedSize];
        decompress(src, 0, src.length, dst, 0, uncompressedSize);
        return ByteBuffer.wrap(dst);
    }

    /** Compresses data to LZ4 block format. */
    public static ByteBuffer compress(ByteBuffer uncompressed, int uncompressedSize) {
        byte[] src = toArray(uncompressed);
        byte[] dst = new byte[maxCompressedLength(uncompressedSize)];
        int compressedLen = compress(src, 0, uncompressedSize, dst, 0);
        return ByteBuffer.wrap(dst, 0, compressedLen);
    }

    /** Returns the maximum compressed length for an input of the given size. */
    public static int maxCompressedLength(int sourceLen) {
        return sourceLen + (sourceLen / 255) + 16;
    }

    // -------------------------------------------------------------------------

    private static byte[] toArray(ByteBuffer buf) {
        if (buf.hasArray() && buf.arrayOffset() == 0 && buf.position() == 0) {
            return buf.array();
        }
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        return arr;
    }

    private static void decompress(byte[] src, int srcOff, int srcLen,
                                   byte[] dst, int dstOff, int dstLen) throws IOException {
        final int srcEnd = srcOff + srcLen;
        final int dstEnd = dstOff + dstLen;
        int sp = srcOff, dp = dstOff;

        while (sp < srcEnd) {
            int token = src[sp++] & 0xFF;

            // --- literals ---
            int litLen = token >>> 4;
            if (litLen == 15) {
                int s;
                do {
                    if (sp >= srcEnd) throw new IOException("LZ4: unexpected end (literal length)");
                    s = src[sp++] & 0xFF;
                    litLen += s;
                } while (s == 255);
            }
            if (dp + litLen > dstEnd) throw new IOException("LZ4: output overflow (literals)");
            System.arraycopy(src, sp, dst, dp, litLen);
            sp += litLen;
            dp += litLen;

            if (sp >= srcEnd) break; // last sequence ends with literals only

            // --- match ---
            if (sp + 1 >= srcEnd) throw new IOException("LZ4: truncated match offset");
            int offset = (src[sp] & 0xFF) | ((src[sp + 1] & 0xFF) << 8);
            sp += 2;
            if (offset == 0) throw new IOException("LZ4: invalid match offset 0");

            int matchLen = (token & 0x0F) + MIN_MATCH;
            if (matchLen == MIN_MATCH + 15) {
                int s;
                do {
                    if (sp >= srcEnd) throw new IOException("LZ4: unexpected end (match length)");
                    s = src[sp++] & 0xFF;
                    matchLen += s;
                } while (s == 255);
            }

            int matchPos = dp - offset;
            if (matchPos < dstOff) throw new IOException("LZ4: invalid match position");
            if (dp + matchLen > dstEnd) throw new IOException("LZ4: output overflow (match)");
            // Overlapping matches are intentional (repeat pattern) — copy byte-by-byte
            for (int i = 0; i < matchLen; i++) dst[dp + i] = dst[matchPos + i];
            dp += matchLen;
        }
    }

    private static int compress(byte[] src, int srcOff, int srcLen, byte[] dst, int dstOff) {
        final int[] hashTable = new int[HASH_SIZE];
        Arrays.fill(hashTable, -1);

        final int srcEnd     = srcOff + srcLen;
        final int srcEndMF   = srcEnd - LAST_LITERALS - MIN_MATCH;
        int sp = srcOff, dp = dstOff, literalStart = srcOff;

        while (sp <= srcEndMF) {
            int v = readInt(src, sp);
            int h = hash(v);
            int matchPos = hashTable[h];
            hashTable[h] = sp;

            if (matchPos >= 0 && matchPos >= sp - 65535 && readInt(src, matchPos) == v) {
                // Found a match — emit literal run + match sequence
                int litLen = sp - literalStart;
                int tokenPos = dp++;

                // extra literal-length bytes
                int litExtra = litLen - 15;
                if (litLen >= 15) {
                    while (litExtra >= 255) { dst[dp++] = (byte) 255; litExtra -= 255; }
                    dst[dp++] = (byte) litExtra;
                }
                // literals
                System.arraycopy(src, literalStart, dst, dp, litLen);
                dp += litLen;

                // match length
                int matchLen = MIN_MATCH;
                int limit = Math.min(srcEnd - LAST_LITERALS, sp + 65535);
                while (sp + matchLen < limit && src[matchPos + matchLen] == src[sp + matchLen]) matchLen++;

                // match offset (LE)
                int offset = sp - matchPos;
                dst[dp++] = (byte) offset;
                dst[dp++] = (byte) (offset >>> 8);

                // token = (min(litLen,15)<<4) | min(matchLen-MIN_MATCH, 15)
                int matchExtra = matchLen - MIN_MATCH;
                dst[tokenPos] = (byte) (((Math.min(litLen, 15)) << 4) | Math.min(matchExtra, 15));

                // extra match-length bytes
                if (matchExtra >= 15) {
                    matchExtra -= 15;
                    while (matchExtra >= 255) { dst[dp++] = (byte) 255; matchExtra -= 255; }
                    dst[dp++] = (byte) matchExtra;
                }

                sp += matchLen;
                literalStart = sp;
            } else {
                sp++;
            }
        }

        // Final literal run (no trailing match allowed)
        int litLen = srcEnd - literalStart;
        int tokenPos = dp++;
        int litExtra = litLen - 15;
        if (litLen >= 15) {
            while (litExtra >= 255) { dst[dp++] = (byte) 255; litExtra -= 255; }
            dst[dp++] = (byte) litExtra;
        }
        dst[tokenPos] = (byte) (Math.min(litLen, 15) << 4);
        System.arraycopy(src, literalStart, dst, dp, litLen);
        dp += litLen;

        return dp - dstOff;
    }

    private static int readInt(byte[] data, int offset) {
        return (data[offset]     & 0xFF)
             | ((data[offset+1] & 0xFF) << 8)
             | ((data[offset+2] & 0xFF) << 16)
             | ((data[offset+3] & 0xFF) << 24);
    }

    private static int hash(int v) {
        return (v * 0x9E3779B1) >>> (32 - HASH_LOG);
    }

    private LZ4Block() {}
}
