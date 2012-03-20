package com.tomgibara.crinch.coding;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;

public final class GolombCoding extends UniversalCoding {

	private final int divisor;
	private final BigInteger bigDivisor;
	private final TruncatedBinaryCoding truncated;
	
	public GolombCoding(int divisor) {
		if (divisor < 1) throw new IllegalArgumentException("non-positive divisor");
		this.divisor = divisor;
		this.bigDivisor = BigInteger.valueOf(divisor);
		truncated = new TruncatedBinaryCoding(bigDivisor);
	}
	
	public int getDivisor() {
		return divisor;
	}
	
	@Override
	int unsafeEncodePositiveInt(BitWriter writer, int value) {
		int q = value / divisor;
		int r = value - q * divisor;
		return
		UnaryCoding.zeroTerminated.unsafeEncodePositiveInt(writer, q)
		+ truncated.encodeInt(writer, r);
	}

	@Override
	int unsafeEncodePositiveLong(BitWriter writer, long value) {
		long q = value / divisor;
		long r = value - q * divisor;
		return
		UnaryCoding.zeroTerminated.unsafeEncodePositiveLong(writer, q)
		+ truncated.encodeLong(writer, r);
	}

	@Override
	int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
		BigInteger[] qr = value.divideAndRemainder(bigDivisor);
		return
		UnaryCoding.zeroTerminated.unsafeEncodePositiveBigInt(writer, qr[0])
		+ truncated.encodeBigInt(writer, qr[1]);
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		return
		UnaryCoding.zeroTerminated.decodePositiveInt(reader) * divisor
		+ truncated.decodeInt(reader);
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		return
		UnaryCoding.zeroTerminated.decodePositiveLong(reader) * divisor
		+ truncated.decodeLong(reader);
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		return
		UnaryCoding.zeroTerminated.decodePositiveBigInt(reader)
		.multiply(bigDivisor)
		.add(truncated.decodeBigInt(reader));
	}

}
