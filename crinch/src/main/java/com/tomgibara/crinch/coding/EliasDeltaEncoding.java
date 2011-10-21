/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;


public class EliasDeltaEncoding {

    //can only encode strictly positive values
    public static int encode(int value, BitWriter writer) {
        if (value == 0) throw new IllegalArgumentException();
        int size = 32 - Integer.numberOfLeadingZeros(value); //position of leading 1
        int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
        int count = 0;
        count += writer.writeZeros(sizeLength -1);
        count += writer.write(size, sizeLength);
        count += writer.write(value, size - 1);
        return count;
        
    }
    
    //can only encode strictly positive values
    public static int encodeLong(long value, BitWriter writer) {
        if (value == 0) throw new IllegalArgumentException();
        int size = 64 - Long.numberOfLeadingZeros(value); //position of leading 1
        int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
        int count = 0;
        count += writer.writeZeros(sizeLength -1);
        count += writer.write(size, sizeLength);
        count += writer.write(value, size - 1);
        return count;
        
    }
    public static int decode(BitReader reader) {
        int sizeLength = 0;
        while (reader.readBit() == 0) sizeLength++;
        if (sizeLength == 0) return 1;
        int size = (1 << sizeLength) | reader.read(sizeLength);
        int x = reader.read(size - 1);
        return (1 << (size-1)) | x;
    }
    
}
