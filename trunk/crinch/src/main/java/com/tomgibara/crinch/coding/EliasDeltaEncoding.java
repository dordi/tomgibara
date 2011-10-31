/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.coding;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;


public final class EliasDeltaEncoding extends AbstractCoding {

	// statics
	
	public static final EliasDeltaEncoding instance = new EliasDeltaEncoding();
	public static final ExtendedCoding extended = new ExtendedCoding(instance);
	
    // constructors
    
    private EliasDeltaEncoding() { }

    // abstract methods
    
    @Override
    int unsafeEncodePositiveInt(BitWriter writer, int value) {
        int size = 32 - Integer.numberOfLeadingZeros(value); //position of leading 1
        int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
        int count = 0;
        count += writer.writeZeros(sizeLength -1);
        count += writer.write(size, sizeLength);
        count += writer.write(value, size - 1);
        return count;
        
    }
    
    @Override
    int unsafeEncodePositiveLong(BitWriter writer, long value) {
        int size = 64 - Long.numberOfLeadingZeros(value); //position of leading 1
        int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
        int count = 0;
        count += writer.writeZeros(sizeLength -1);
        count += writer.write(size, sizeLength);
        count += writer.write(value, size - 1);
        return count;
    }
    
    @Override
    int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
    	int size = value.bitLength();
        int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
        int count = 0;
        count += writer.writeZeros(sizeLength -1);
        count += writer.write(size, sizeLength);
        count += writer.write(value, size - 1);
        return count;
    }
    
    // coding methods
    
	@Override
	public int encodePositiveInt(BitWriter writer, int value) {
		if (value <= 0) throw new IllegalArgumentException("non-positive value");
		return unsafeEncodePositiveInt(writer, value);
	}

	@Override
	public int encodePositiveLong(BitWriter writer, long value) {
		if (value <= 0L) throw new IllegalArgumentException("non-positive value");
		return unsafeEncodePositiveLong(writer, value);
	}

	@Override
	public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
		if (value == null) throw new IllegalArgumentException("null value");
		if (value.signum() != 1) throw new IllegalArgumentException("non-positive value");
		return unsafeEncodePositiveBigInt(writer, value);
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
        int sizeLength = 0;
        while (!reader.readBoolean()) sizeLength++;
        if (sizeLength == 0) return 1;
        int size = (1 << sizeLength) | reader.read(sizeLength);
        int x = reader.read(size - 1);
        return (1 << (size-1)) | x;
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
        int sizeLength = 0;
        while (!reader.readBoolean()) sizeLength++;
        if (sizeLength == 0) return 1L;
        int size = (1 << sizeLength) | reader.read(sizeLength);
        long x = reader.readLong(size - 1);
        return (1L << (size-1)) | x;
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
        int sizeLength = 0;
        while (!reader.readBoolean()) sizeLength++;
        if (sizeLength == 0) return BigInteger.ONE;
        int size = (1 << sizeLength) | reader.read(sizeLength);
        BigInteger x = reader.readBigInt(size - 1);
        return x.or(BigInteger.ONE.shiftLeft(size - 1));
	}
    
}
