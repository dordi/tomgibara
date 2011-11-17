package com.tomgibara.crinch.coding;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitWriter;

public abstract class UniversalCoding implements Coding {

	abstract int unsafeEncodePositiveInt(BitWriter writer, int value);
	
	abstract int unsafeEncodePositiveLong(BitWriter writer, long value);
	
	abstract int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value);

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

}
