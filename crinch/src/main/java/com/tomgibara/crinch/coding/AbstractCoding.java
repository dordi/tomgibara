package com.tomgibara.crinch.coding;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitWriter;

public abstract class AbstractCoding implements Coding {

	abstract int unsafeEncodePositiveInt(BitWriter writer, int value);
	
	abstract int unsafeEncodePositiveLong(BitWriter writer, long value);
	
	abstract int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value);

}
