package com.tomgibara.crinch.coding;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;

public interface Coding {

	int encodePositiveInt(BitWriter writer, int value);
	
	int encodePositiveLong(BitWriter writer, long value);
	
	int encodePositiveBigInt(BitWriter writer, BigInteger value);
	
	int decodePositiveInt(BitReader reader);
	
	long decodePositiveLong(BitReader reader);
	
	BigInteger decodePositiveBigInt(BitReader reader);
	
}
