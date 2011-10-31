package com.tomgibara.crinch.coding;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitWriter;

public class CodedWriter {

	// fields
	
	private final BitWriter writer;
	private final ExtendedCoding coding;
	
	// constructors
	
	public CodedWriter(BitWriter writer, ExtendedCoding coding) {
		if (writer == null) throw new IllegalArgumentException("null writer");
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.writer = writer;
		this.coding = coding;
	}
	
	// accessors
	
	public BitWriter getWriter() {
		return writer;
	}
	
	public ExtendedCoding getCoding() {
		return coding;
	}
	
	// methods
	
	public int writePositiveInt(int value) {
		return coding.encodePositiveInt(writer, value);
	}
	
	public int writePositiveLong(long value) {
		return coding.encodePositiveLong(writer, value);
	}
	
	public int writePositiveBigInt(BigInteger value) {
		return coding.encodePositiveBigInt(writer, value);
	}
	
	public int writeSignedInt(int value) {
		return coding.encodeSignedInt(writer, value);
	}
	
	public int writeSignedLong(long value) {
		return coding.encodeSignedLong(writer, value);
	}
	
	public int writeSignedBigInt(BigInteger value) {
		return coding.encodeSignedBigInt(writer, value);
	}
	
	public int writeDouble(double value) {
		return coding.encodeDouble(writer, value);
	}
	
	public int writeDecimal(BigDecimal value) {
		return coding.encodeDecimal(writer, value);
	}
	
}
