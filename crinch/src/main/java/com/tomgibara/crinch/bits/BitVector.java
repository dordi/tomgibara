package com.tomgibara.crinch.bits;

import java.util.Arrays;

public final class BitVector implements Cloneable {

	// statics
	
	public enum Operation {
		SET,
		AND,
		OR,
		XOR
	}
	
	private static final int SET = 0;
	private static final int AND = 1;
	private static final int OR  = 2;
	private static final int XOR = 3;

	private static final int ADDRESS_BITS = 6;
	private static final int ADDRESS_SIZE = 1 << ADDRESS_BITS;
	private static final int ADDRESS_MASK = ADDRESS_SIZE - 1;
	
	//necessary for throwing an IAE
	private static int stringLength(String str) {
		if (str == null) throw new IllegalArgumentException();
		return str.length();
	}
	
	// fields
	
	private final int size;
	private final long[] bits;
	private final long mask;
	
	// constructors
	
	public BitVector(int size) {
		if (size < 0) throw new IllegalArgumentException();
		final int length = (size + ADDRESS_MASK) >> ADDRESS_BITS;
		this.bits = new long[length];
		this.size = size;
		this.mask = -1L >>> (length * ADDRESS_SIZE - size) ;
	}
	
	public BitVector(BitVector that) {
		this.size = that.size;
		this.bits = that.bits.clone();
		this.mask = that.mask;
	}
	
	public BitVector(String str) {
		this(stringLength(str));
		//TODO can this be usefully optimized?
		for (int i = 0; i < size; i++) {
			final char c = str.charAt(i);
			if (c == '1') set(i, true);
			else if (c != '0') throw new IllegalArgumentException("Illegal character '" + c + "' at index " + i + ", expected '0' or '1'.");
		}
	}
	
	// accessors
	
	public int getSize() {
		return size;
	}

	// getters
	
	public boolean getBit(int position) {
		if (position < 0 || position >= size) throw new IllegalArgumentException();
		final int i = (size - 1 - position) >> ADDRESS_BITS;
		final long m = 1L << (position & ADDRESS_MASK);
		return (bits[i] & m) != 0;
	}
	
	public byte getByte(int position) {
		return (byte) getBits(position, 8);
	}

	public short getShort(int position) {
		return (byte) getBits(position, 16);
	}
	
	public int getInt(int position) {
		return (int) getBits(position, 32);
	}
	
	public long getLong(int position) {
		return (int) getBits(position, 64);
	}
	
	public long getBits(int position, int length) {
		if (position < 0 || position > size - length) throw new IllegalArgumentException();
		if (length < 0) throw new IllegalArgumentException();
		if (length == 0) return 0L;
		final int i = (size - 1 - position) >> ADDRESS_BITS;
		final int s = position & ADDRESS_MASK;
		final long b;
		if (s == 0) { // fast case, long-aligned
			b = bits[i];
		} else if (s + length <= ADDRESS_SIZE) { //single long case
			b = bits[i] >>> s;
		} else {
			b = (bits[i] >>> s) | (bits[i+1] << (s + length - ADDRESS_SIZE));
		}
		return length == ADDRESS_SIZE ? b : b & ((1 << length) - 1);
	}
	
	// operations
	
	public void perform(Operation operation, boolean value) {
		perform(operation.ordinal(), value);
	}
	
	public void perform(Operation operation, int position, boolean value) {
		perform(operation.ordinal(), position, value);
	}
	
	public void perform(Operation operation, BitVector vector) {
		perform(operation.ordinal(), vector);
	}
	
	// convenience
	
	public void set(boolean value) {
		perform(SET, value);
	}
	
	public void set(int position, boolean value) {
		perform(SET, position, value);
	}

	public void set(BitVector vector) {
		perform(SET, vector);
	}
	
	public void and(boolean value) {
		perform(AND, value);
	}
	
	public void and(int position, boolean value) {
		perform(AND, position, value);
	}
	
	public void and(BitVector vector) {
		perform(AND, vector);
	}
	
	public void or(boolean value) {
		perform(OR, value);
	}
	
	public void or(int position, boolean value) {
		perform(OR, position, value);
	}

	public void or(BitVector vector) {
		perform(OR, vector);
	}
	
	public void xor(boolean value) {
		perform(XOR, value);
	}
	
	public void xor(int position, boolean value) {
		perform(XOR, position, value);
	}

	public void xor(BitVector vector) {
		perform(XOR, vector);
	}
	
	// tests
	
	public boolean contains(BitVector that) {
		if (that.size != this.size) throw new IllegalArgumentException();
		long[] thisBits = this.bits;
		long[] thatBits = that.bits;
		for (int i = 0; i < thisBits.length; i++) {
			final long bits = thisBits[i];
			if ((bits | thatBits[i]) != bits) return false;
		}
		return true;
	}
	
	public boolean intersects(BitVector that) {
		if (that.size != this.size) throw new IllegalArgumentException();
		long[] thisBits = this.bits;
		long[] thatBits = that.bits;
		for (int i = 0; i < thisBits.length; i++) {
			if ((thisBits[i] & thatBits[i]) != 0L) return true;
		}
		return false;
	}
	
	// object methods
	
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BitVector)) return false;
		final BitVector that = (BitVector) obj;
		if (this.size != that.size) return false;
		for (int i = 0; i < this.bits.length; i++) {
			if (this.bits[i] != that.bits[i]) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < bits.length; i++) {
			final long l = bits[i];
			h ^= ((int) l       ) * 31;
			h ^= ((int)(l >> 32)) * 31;
		}
		return h;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			sb.append(getBit(i) ? '1' : '0');
		}
		return sb.toString();
	}
	
	public BitVector clone() {
		try {
			return (BitVector) super.clone();
		} catch (CloneNotSupportedException e) {
			//should never occur
			throw new RuntimeException("Clone failure!", e);
		}
	}
	
	// private utility methods

	private void perform(int operation, int position, boolean value) {
		if (position < 0 || position >= size) throw new IllegalArgumentException();
		final int i = (size - 1 - position) >> ADDRESS_BITS;
		final long m = 1L << (position & ADDRESS_MASK);
		switch(operation) {
		case SET : 
			if (value) {
				bits[i] |=  m;
			} else {
				bits[i] &= ~m;
			}
			break;
		case AND :
			if (value) {
				/* no-op */
			} else {
				bits[i] &= ~m;
			}
			break;
		case OR :
			if (value) {
				bits[i] |=  m;
			} else {
				/* no-op */
			}
			break;
		case XOR :
			if (value) {
				bits[i] ^=  m;
			} else {
				/* no-op */
			}
			break;
		}
	}
	
	private void perform(int operation, boolean value) {
		switch (operation) {
		case AND: if (value == false) perform(SET, false); else return; 
		case OR:  if (value == true) perform(SET, true); else return;
		case XOR : if (value == false) return;
		}
		final long m = value ? -1L : 0L;
		if (operation == SET) {
			Arrays.fill(bits, m);
		} else {
			for (int i = 0; i < bits.length; i++) bits[i] &= m;
		}
		if (value) bits[bits.length - 1] &= mask;
	}
	
	private void perform(int operation, BitVector that) {
		if (that == null) throw new IllegalArgumentException("null vector");
		if (this.size != that.size) throw new IllegalArgumentException("incorrect size, expected " + this.size + " and got " + that.size);
		long[] thisBits = this.bits;
		long[] thatBits = that.bits;
		switch (operation) {
		case SET :
			System.arraycopy(thatBits, 0, thisBits, 0, thatBits.length);
			break;
		case AND :
			for (int i = 0; i < thisBits.length; i++) thisBits[i] &= thatBits[i];
			break;
		case OR:
			for (int i = 0; i < thisBits.length; i++) thisBits[i] |= thatBits[i];
			break;
		case XOR:
			for (int i = 0; i < thisBits.length; i++) thisBits[i] ^= thatBits[i];
			break;
		}
	}
	
}
