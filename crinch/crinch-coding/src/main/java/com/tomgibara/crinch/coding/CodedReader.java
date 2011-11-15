package com.tomgibara.crinch.coding;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;

public class CodedReader {

	// fields
	
	private final BitReader reader;
	private final ExtendedCoding coding;
	
	// constructors
	
	public CodedReader(BitReader reader, ExtendedCoding coding) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.reader = reader;
		this.coding = coding;
	}
	
	// accessors
	
	public BitReader getReader() {
		return reader;
	}
	
	public ExtendedCoding getCoding() {
		return coding;
	}
	
	// methods
	
	public int readPositiveInt() {
		return coding.decodePositiveInt(reader);
	}
	
	public long readPositiveLong() {
		return coding.decodePositiveLong(reader);
	}
	
	public BigInteger readPositiveBigInt() {
		return coding.decodePositiveBigInt(reader);
	}
	
	public int readSignedInt() {
		return coding.decodeSignedInt(reader);
	}
	
	public long readSignedLong() {
		return coding.decodeSignedLong(reader);
	}
	
	public BigInteger readSignedBigInt() {
		return coding.decodeSignedBigInt(reader);
	}
	
	public double readDouble() {
		return coding.decodeDouble(reader);
	}
	
	public BigDecimal readDecimal() {
		return coding.decodeDecimal(reader);
	}
	
}
