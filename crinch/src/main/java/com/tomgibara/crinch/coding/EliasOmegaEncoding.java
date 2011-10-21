/*
 * Copyright (C) 2007  Tom Gibara
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.tomgibara.crinch.coding;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;

public class EliasOmegaEncoding {

	private static int encode0(int value, BitWriter writer) {
		if (value == 1) return 0;
        int size = 32 - Integer.numberOfLeadingZeros(value); //position of leading 1
        return encode0(size-1, writer) + writer.write(value, size);
	}
	
    //can only encode strictly positive values
    public static int encode(int value, BitWriter writer) {
        if (value == 0) throw new IllegalArgumentException();
    	int c = encode0(value, writer);
        writer.writeBit(0);
        return c + 1;
    }

	private static int encodeLong0(long value, BitWriter writer) {
		if (value == 1L) return 0;
        int size = 64 - Long.numberOfLeadingZeros(value); //position of leading 1
        return encodeLong0(size-1, writer) + writer.write(value, size);
	}
	
    //can only encode strictly positive values
    public static int encodeLong(long value, BitWriter writer) {
        if (value == 0L) throw new IllegalArgumentException();
    	int c = encodeLong0(value, writer);
        writer.writeBit(0);
        return c + 1;
    }

    public static int decode(BitReader reader) {
    	int value = 1;
    	while (true) {
	    	int b = reader.readBit();
	    	if (b == 0) return value;
	    	value = (1 << value) | reader.read(value);
    	}
    }
    
    public static long decodeLong(BitReader reader) {
    	long value = 1;
    	while (true) {
	    	int b = reader.readBit();
	    	if (b == 0) return value;
	    	value = (1 << value) | reader.readLong((int)value);
    	}
    }
    
}
