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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitStreamException;
import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.bits.BitWriter;

public class EliasOmegaEncoding {

	private static int encodeInt0(int value, BitWriter writer) {
		if (value == 1) return 0;
        int size = 32 - Integer.numberOfLeadingZeros(value); //position of leading 1
        return encodeInt0(size-1, writer) + writer.write(value, size);
	}
	
	private static int encodeLong0(long value, BitWriter writer) {
		if (value == 1L) return 0;
        int size = 64 - Long.numberOfLeadingZeros(value); //position of leading 1
        return encodeInt0(size-1, writer) + writer.write(value, size);
	}

	private static int encodeBigInt0(BigInteger value, BitWriter writer) {
		if (value.equals(BigInteger.ONE)) return 0;
		int size = value.bitLength();
		return encodeInt0(size - 1, writer) + writer.write(value, size);
	}
	
    public static int encodePositiveInt(int value, BitWriter writer) {
        if (value <= 0) throw new IllegalArgumentException();
    	return encodeInt0(value, writer) + writer.writeBit(0);
    }

    public static int encodePositiveLong(long value, BitWriter writer) {
        if (value <= 0L) throw new IllegalArgumentException();
    	return encodeLong0(value, writer) + writer.writeBit(0);
    }

    public static int encodePositiveBigInt(BigInteger value, BitWriter writer) {
        if (value.signum() != 1) throw new IllegalArgumentException();
    	return encodeBigInt0(value, writer) + writer.writeBit(0);
    }

    public static int decodePositiveInt(BitReader reader) {
    	int value = 1;
    	while (reader.readBoolean()) {
	    	value = (1 << value) | reader.read(value);
    	}
    	return value;
    }

    public static long decodePositiveLong(BitReader reader) {
    	long value = 1;
    	while (reader.readBoolean()) {
	    	value = (1L << (int)value) | reader.readLong((int)value);
    	}
    	return value;
    }
    
    public static BigInteger decodePositiveBigInt(BitReader reader) {
    	int value = 1;
    	while (reader.readBoolean()) {
    		if (value < 32) {
    	    	value = (1 << value) | reader.read(value);
    		} else {
    			BitVector vector = new BitVector(value + 1);
    			vector.setBit(value, true);
    			reader.readBits(vector.rangeView(0, value));
    			if (reader.readBoolean()) throw new BitStreamException("value too large for BigInteger");
    			return vector.toBigInteger();
    		}
    	}
    	return BigInteger.valueOf(value & 0xffffffffL);
    }
    
    public static int encodeSignedInt(int value, BitWriter writer) {
    	value = value > 0 ? value << 1 : 1 - (value << 1);
    	return encodeInt0(value, writer) + writer.writeBit(0);
    }

    public static int decodeSignedInt(BitReader reader) {
    	int value = decodePositiveInt(reader);
    	// the term ... | (value & (1 << 31) serves to restore sign bit
    	// in the special case where decoding overflows
    	// but we have enough info to reconstruct the correct value
   		return (value & 1) == 1 ? ((1 - value) >> 1) | (value & (1 << 31)) : value >>> 1;
    }
    
    public static int encodeSignedLong(long value, BitWriter writer) {
    	value = value > 0L ? value << 1 : 1L - (value << 1);
    	return encodeLong0(value, writer) + writer.writeBit(0);
    }

    public static long decodeSignedLong(BitReader reader) {
    	long value = decodePositiveLong(reader);
    	// see comments in decodeSignedInt
   		return (value & 1L) == 1L ? ((1L - value) >> 1) | (value & (1L << 63)) : value >>> 1;
    }
    
    public static int encodeSignedBigInt(BigInteger value, BitWriter writer) {
    	value = value.signum() == 1 ? value = value.shiftLeft(1) : BigInteger.ONE.subtract(value.shiftLeft(1));
    	return encodeBigInt0(value, writer) + writer.writeBit(0); 
    }
    
    public static BigInteger decodeSignedBigInt(BitReader reader) {
    	BigInteger value = decodePositiveBigInt(reader);
    	return value.testBit(0) ? BigInteger.ONE.subtract(value).shiftRight(1) : value.shiftRight(1);
    }
    
    //TODO move off object - when methods are not static, can delegate
    public static int encodeDouble(double value, BitWriter writer) {
    	if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException();
    	long bits = Double.doubleToLongBits(value);
    	long sign = bits & 0x8000000000000000L;
    	if (sign == bits) return encodePositiveInt(sign == 0L ? 1 : 2, writer);
    	
    	long mantissa = bits & 0x000fffffffffffffL;
		if (sign == 0) {
			mantissa = (mantissa << 1) + 3L;
		} else {
			mantissa = (mantissa << 1) + 4L;
		}
		int exponent = (int) ((bits & 0x7ff0000000000000L) >> 52) - 1023;
    	return encodePositiveLong(mantissa, writer) + encodeSignedInt(exponent, writer);
    }
    
    public static double decodeDouble(BitReader reader) {
    	long mantissa = decodePositiveLong(reader);
    	if (mantissa == 1L) return 0.0;
    	if (mantissa == 2L) return -0.0;
    	int exponent = decodeSignedInt(reader);
    	long bits = (exponent + 1023L) << 52;
    	if ((mantissa & 1L) == 0) {
    		bits |= 0x8000000000000000L;
    		mantissa = (mantissa - 4L) >> 1;
    	} else {
    		mantissa = (mantissa - 3L) >> 1;
    	}
    	bits |= mantissa;
    	return Double.longBitsToDouble(bits);
    }

    public static int encodeDecimal(BigDecimal value, BitWriter writer) {
    	return encodeSignedInt(value.scale(), writer) + encodeSignedBigInt(value.unscaledValue(), writer);
    }
    
    public static BigDecimal decodeDecimal(BitReader reader) {
    	int scale = decodeSignedInt(reader);
    	BigInteger bigInt = decodeSignedBigInt(reader);
    	return new BigDecimal(bigInt, scale);
    }
    
}
