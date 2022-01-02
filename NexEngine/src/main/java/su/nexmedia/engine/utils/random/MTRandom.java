package su.nexmedia.engine.utils.random;

import java.util.Random;

public class MTRandom extends Random {

    private static final long    serialVersionUID = -515082678588212038L;
    private static final int     UPPER_MASK       = Integer.MIN_VALUE;
    private static final int     LOWER_MASK       = Integer.MAX_VALUE;
    private static final int     N                = 624;
    private static final int     M                = 397;
    private static final int[]   MAGIC;
    private static final int     MAGIC_FACTOR1    = 1812433253;
    private static final int     MAGIC_FACTOR2    = 1664525;
    private static final int     MAGIC_FACTOR3    = 1566083941;
    // private static final int MAGIC_MASK1 = -1658038656;
    // private static final int MAGIC_MASK2 = -272236544;
    private static final int     MAGIC_SEED       = 19650218;
    private static final long    DEFAULT_SEED     = 5489L;

    static {
        MAGIC = new int[]{0, -1727483681};
    }

    private transient    int[]   mt;
    private transient    int     mti;
    private transient    boolean compat;
    private transient    int[]   ibuf;

    public MTRandom() {
        this(false);
    }

    public MTRandom(boolean compatible) {
        super(0L);
        this.compat = false;
        this.compat = compatible;
        this.setSeed(this.compat ? DEFAULT_SEED : System.currentTimeMillis());
    }

    public MTRandom(long seed) {
        super(seed);
        this.compat = false;
    }

    public MTRandom(byte[] buf) {
        super(0L);
        this.compat = false;
        this.setSeed(buf);
    }

    public MTRandom(int[] buf) {
        super(0L);
        this.compat = false;
        this.setSeed(buf);
    }

    public static int[] pack(byte[] buf) {
        int blen = buf.length;
        int ilen = buf.length + 3 >>> 2;
        int[] ibuf = new int[ilen];
        for (int n = 0; n < ilen; ++n) {
            int m = n + 1 << 2;
            if (m > blen) {
                m = blen;
            }
            int k;
            for (k = (buf[--m] & 0xFF); (m & 0x3) != 0x0; k = (k << 8 | (buf[--m] & 0xFF))) {
            }
            ibuf[n] = k;
        }
        return ibuf;
    }

    private void setSeed(int seed) {
        if (this.mt == null) {
            this.mt = new int[N];
        }
        this.mt[0] = seed;
        this.mti = 1;
        while (this.mti < N) {
            this.mt[this.mti] = MAGIC_FACTOR1 * (this.mt[this.mti - 1] ^ this.mt[this.mti - 1] >>> 30) + this.mti;
            ++this.mti;
        }
    }

    @Override
    public synchronized void setSeed(long seed) {
        if (this.compat) {
            this.setSeed((int) seed);
        }
        else {
            if (this.ibuf == null) {
                this.ibuf = new int[2];
            }
            this.ibuf[0] = (int) seed;
            this.ibuf[1] = (int) (seed >>> 32);
            this.setSeed(this.ibuf);
        }
    }

    public void setSeed(byte[] buf) {
        this.setSeed(pack(buf));
    }

    public synchronized void setSeed(int[] buf) {
        int length = buf.length;
        if (length == 0) {
            throw new IllegalArgumentException("Seed buffer may not be empty");
        }
        int i = 1;
        int j = 0;
        int k = (N > length) ? N : length;
        this.setSeed(MAGIC_SEED);
        while (k > 0) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * MAGIC_FACTOR2) + buf[j] + j;
            ++i;
            ++j;
            if (i >= N) {
                this.mt[0] = this.mt[623];
                i = 1;
            }
            if (j >= length) {
                j = 0;
            }
            --k;
        }
        for (k = 623; k > 0; --k) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * MAGIC_FACTOR3) - i;
            if (++i >= N) {
                this.mt[0] = this.mt[623];
                i = 1;
            }
        }
        this.mt[0] = UPPER_MASK;
    }

    @Override
    protected synchronized int next(int bits) {
        if (this.mti >= N) {
            int kk;
            for (kk = 0; kk < 227; ++kk) {
                int y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
                this.mt[kk] = (this.mt[kk + M] ^ y >>> 1 ^ MTRandom.MAGIC[y & 0x1]);
            }
            while (kk < 623) {
                int y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
                this.mt[kk] = (this.mt[kk - 227] ^ y >>> 1 ^ MTRandom.MAGIC[y & 0x1]);
                ++kk;
            }
            int y = (this.mt[623] & UPPER_MASK) | (this.mt[0] & LOWER_MASK);
            this.mt[623] = (this.mt[396] ^ y >>> 1 ^ MTRandom.MAGIC[y & 0x1]);
            this.mti = 0;
        }
        int y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= (y << 7 & 0x9D2C5680);
        y ^= (y << 15 & 0xEFC60000);
        y ^= y >>> 18;
        return y >>> 32 - bits;
    }
}
