package com.tomgibara.crinch.coding;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitStreamException;
import com.tomgibara.crinch.bits.BitWriter;

public class UnaryCoding extends UniversalCoding {

	// statics

	public static final int MAX_ENCODABLE_INT = Integer.MAX_VALUE - 1;
	public static final BigInteger MAX_ENCODABLE_BIG_INT = BigInteger.valueOf(Integer.MAX_VALUE - 1);
	
	//TODO find better names
	public static final UnaryCoding zeroTerminated = new UnaryCoding(false);
	public static final UnaryCoding oneTerminated = new UnaryCoding(true);
	public static final ExtendedCoding zeroExtended = new ExtendedCoding(zeroTerminated);
	public static final ExtendedCoding oneExtended = new ExtendedCoding(oneTerminated);
	
	private final boolean terminalBit;
	
	public UnaryCoding(boolean terminalBit) {
		this.terminalBit = terminalBit;
	}
	
	public boolean isTerminatedByOne() {
		return terminalBit;
	}
	
	@Override
	int unsafeEncodePositiveInt(BitWriter writer, int value) {
		int count = (int) writer.writeBooleans(!terminalBit, value);
		count += writer.writeBoolean(terminalBit);
		return count;
	}

	@Override
	int unsafeEncodePositiveLong(BitWriter writer, long value) {
		// we can't support returning this many bits in count
		// and with unary encoding, it's hard to imagine a scenario for this
		if (value > MAX_ENCODABLE_INT) throw new IllegalArgumentException("value exceeds maximum encodable value");
		return unsafeEncodePositiveInt(writer, (int) value);
	}

	@Override
	int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
		// see comments above
		if (value.compareTo(MAX_ENCODABLE_BIG_INT) > 0) throw new IllegalArgumentException("value exceeds maximum encodable value");
		return unsafeEncodePositiveInt(writer, value.intValue());
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		long count = reader.readUntil(terminalBit);
		//TODO should have a separate DecodingException?
		if (count > Integer.MAX_VALUE) throw new BitStreamException("value too large for int");
		return (int) count;
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		return reader.readUntil(terminalBit);
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		return BigInteger.valueOf(reader.readUntil(terminalBit));
	}

}
