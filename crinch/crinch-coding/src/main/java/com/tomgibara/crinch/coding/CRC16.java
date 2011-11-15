package com.tomgibara.crinch.coding;

public final class CRC16 {

	// statics
	
	public static final short DEFAULT_POLY = (short) 0x8005;

	// fields
	
	private final short polynomial;
	private short checksum;

	// constructors
	
	public CRC16() {
		this(DEFAULT_POLY);
	}
	
	public CRC16(short polynomial) {
		this.polynomial = polynomial;
		reset();
	}

	// accessors
	
	short getPolynomial() {
		return polynomial;
	}
	
	// methods
	
	public void addBits(int value, int length) {
		int mask = 1 << (length - 1);
		do {
			checksum <<= 1;
			if (((checksum & 0x8000) == 0) ^ ((value & mask) == 0)) checksum ^= polynomial;
		} while ((mask >>>= 1) != 0);
	}

	public void addInt(int value) {
		addBits(value, 32);
	}

	public void addShort(short value) {
		addBits(value, 16);
	}
	
	public void addByte(byte value) {
		addBits(value, 8);
	}
	
	public short checksum() {
		return checksum;
	}
	
	public void reset() {
		checksum = (short) 0xFFFF;
	}
}