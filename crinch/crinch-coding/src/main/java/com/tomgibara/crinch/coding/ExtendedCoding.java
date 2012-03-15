/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.coding;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;

public class ExtendedCoding implements Coding {

	private static final BigInteger MINUS_ONE = BigInteger.ONE.negate();
	
	// fields
	
	private final UniversalCoding coding;

	// constructor
	
	public ExtendedCoding(UniversalCoding coding) {
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.coding = coding;
	}
	
    // delegated coding methods
    
	@Override
	public int encodePositiveInt(BitWriter writer, int value) {
		return coding.encodePositiveInt(writer, value);
	}

	@Override
	public int encodePositiveLong(BitWriter writer, long value) {
		return coding.encodePositiveLong(writer, value);
	}

	@Override
	public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
		return coding.encodePositiveBigInt(writer, value);
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		return coding.decodePositiveInt(reader);
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		return coding.decodePositiveLong(reader);
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		return coding.decodePositiveBigInt(reader);
	}
    
	// extra methods

    public int encodeSignedInt(BitWriter writer, int value) {
    	value = value < 0 ? -1 - (value << 1) : value << 1;
    	return coding.unsafeEncodePositiveInt(writer, value);
    }

    public int decodeSignedInt(BitReader reader) {
    	int value = decodePositiveInt(reader);
    	// the term ... | (value & (1 << 31) serves to restore sign bit
    	// in the special case where decoding overflows
    	// but we have enough info to reconstruct the correct value
   		return (value & 1) == 1 ? ((-1 - value) >> 1) | (value & (1 << 31)) : value >>> 1;
    }
    
    public int encodeSignedLong(BitWriter writer, long value) {
    	value = value < 0L ? -1L - (value << 1) : value << 1;
    	return coding.unsafeEncodePositiveLong(writer, value);
    }

    public long decodeSignedLong(BitReader reader) {
    	long value = decodePositiveLong(reader);
    	// see comments in decodeSignedInt
   		return (value & 1L) == 1L ? ((-1L - value) >> 1) | (value & (1L << 63)) : value >>> 1;
    }
    
    public int encodeSignedBigInt(BitWriter writer, BigInteger value) {
    	value = value.signum() < 0 ? MINUS_ONE.subtract(value.shiftLeft(1)) : value.shiftLeft(1);
    	return coding.unsafeEncodePositiveBigInt(writer, value);
    }
    
    public BigInteger decodeSignedBigInt(BitReader reader) {
    	BigInteger value = decodePositiveBigInt(reader);
    	return value.testBit(0) ? MINUS_ONE.subtract(value).shiftRight(1) : value.shiftRight(1);
    }
    
    public int encodeDouble(BitWriter writer, double value) {
    	if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException();
    	long bits = Double.doubleToLongBits(value);
    	long sign = bits & 0x8000000000000000L;
    	if (sign == bits) return coding.unsafeEncodePositiveInt(writer, sign == 0L ? 0 : 1);
    	
    	long mantissa = bits & 0x000fffffffffffffL;
		if (sign == 0) {
			mantissa = (mantissa << 1) + 2L;
		} else {
			mantissa = (mantissa << 1) + 3L;
		}
		int exponent = (int) ((bits & 0x7ff0000000000000L) >> 52) - 1023;
    	return coding.unsafeEncodePositiveLong(writer, mantissa) + encodeSignedInt(writer, exponent);
    }
    
    public double decodeDouble(BitReader reader) {
    	long mantissa = decodePositiveLong(reader);
    	if (mantissa == 0L) return 0.0;
    	if (mantissa == 1L) return -0.0;
    	int exponent = decodeSignedInt(reader);
    	long bits = (exponent + 1023L) << 52;
    	if ((mantissa & 1L) == 0) {
    		mantissa = (mantissa - 2L) >> 1;
    	} else {
    		bits |= 0x8000000000000000L;
    		mantissa = (mantissa - 3L) >> 1;
    	}
    	bits |= mantissa;
    	return Double.longBitsToDouble(bits);
    }

    public int encodeDecimal(BitWriter writer, BigDecimal value) {
    	return encodeSignedInt(writer, value.scale()) + encodeSignedBigInt(writer, value.unscaledValue());
    }
    
    public BigDecimal decodeDecimal(BitReader reader) {
    	int scale = decodeSignedInt(reader);
    	BigInteger bigInt = decodeSignedBigInt(reader);
    	return new BigDecimal(bigInt, scale);
    }

}
