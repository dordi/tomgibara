/*
 * Created on 05-Feb-2007
 */
package com.tomgibara.crinch.coding;

import java.util.Arrays;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;


public class FibonacciEncoding {

    //92nd fib no is the largest that is representable with a long
    //because we're starting with 1,2,3,5 - and zero indexed - array index is upto 90
    private static final long[] fib = new long[91];
    
    static {
        fib[0] = 1;
        fib[1] = 2;
        for (int i = 2; i < fib.length; i++) {
            fib[i] = fib[i-1] + fib[i-2];
        }
    }

    private static final int[] fibInt = new int[45];

    static {
        fibInt[0] = 1;
        fibInt[1] = 2;
        for (int i = 2; i < fibInt.length; i++) {
            fibInt[i] = fibInt[i-1] + fibInt[i-2];
        }
    }

    public static void encode(int value, BitWriter writer) {
        int fi = Arrays.binarySearch(fibInt, value);
        if (fi < 0) fi = -2 - fi;
        int count = fi + 3; //one for index adjustment, one for trailing 1, one for leading zero

        int out0 = 0;
        int out1 = 1;
        int offset = 0; //position of last bit written 0 - 31
        boolean o = false; //whether we have overflowed into two ints
        while (fi >= 0) {
            if (++offset == 32) {
                offset = 0;
                o = true;
            }
            long f = fib[fi--];
            if (value >= f) {
                value -= f;
                if (o) {
                    out0  |= 1 << offset;
                } else {
                    out1  |= 1 << offset;
                }
            }
        }
        
        if (++offset == 32) {
            offset = 0;
            o = true;
        }
        
        if (o) {
            writer.write(out0, count-32); writer.write(out1, 32);
        } else {
            writer.write(out1, count);
        }
    }

    public static void encode(long value, BitWriter writer) {
        int fi = Arrays.binarySearch(fib, value);
        if (fi < 0) fi = -2 - fi;
        int count = fi + 3; //one for index adjustment, one for trailing 1, one for leading zero

        int out0 = 0;
        int out1 = 0;
        int out2 = 1;
        int offset = 0; //position of last bit written 0 - 31
        int i = 2;
        while (fi >= 0) {
            if (++offset == 32) {
                offset = 0;
                i--;
            }
            long f = fib[fi--];
            if (value >= f) {
                value -= f;
                switch(i) {
                case 0 : out0  |= 1 << offset; break;
                case 1 : out1  |= 1 << offset; break;
                case 2 : out2  |= 1 << offset; break;
                }
            }
        }

        if (++offset == 32) {
            offset = 0;
            i --;
        }
        
        switch(i) {
        case 0 : writer.write(out0, count-64); writer.write(out1, 32); writer.write(out2, 32); break;
        case 1 : writer.write(out1, count-32); writer.write(out2, 32); break;
        case 2 : writer.write(out2, count); break;
        }
    }

    public static int decode(BitReader reader) {
        int last = reader.readBit();
        if (last != 0) throw new RuntimeException();
        int value = 0;
        for (int i = 0; i <= fibInt.length; i++) {
            int bit = reader.readBit();
            if (bit == 1) {
                if (last == 1) return value;
                value += fibInt[i];
            }
            last = bit;
        }
        return value;
    }
    
    public static long decodeLong(BitReader reader) {
        int last = reader.readBit();
        if (last != 0) throw new RuntimeException();
        long value = 0;
        for (int i = 0; i <= fib.length; i++) {
            int bit = reader.readBit();
            if (bit == 1) {
                if (last == 1) return value;
                value += fib[i];
            }
            last = bit;
        }
        return value;
    }
    
}
