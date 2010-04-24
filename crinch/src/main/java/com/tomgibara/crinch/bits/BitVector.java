package com.tomgibara.crinch.bits;

import java.util.Arrays;

//TODO use case for bloom filters needs immutability
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
			if (c == '1') setBit(size - 1 - i, true);
			else if (c != '0') throw new IllegalArgumentException("Illegal character '" + c + "' at index " + i + ", expected '0' or '1'.");
		}
	}
	
	private BitVector(int size, long[] bits) {
		this.size = size;
		this.bits = bits;
		mask = -1L >>> (bits.length * ADDRESS_SIZE - size) ;
		if (mask != -1L) bits[bits.length - 1] &= mask;
	}
	
	// accessors
	
	public int size() {
		return size;
	}

	// getters
	
	public boolean getBit(int position) {
		if (position < 0 || position >= size) throw new IllegalArgumentException();
		final int i = position >> ADDRESS_BITS;
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
		final int i = position >> ADDRESS_BITS;
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
	
	public BitVector getVector(int position, int length) {
		if (position < 0 || position > size - length) throw new IllegalArgumentException();
		if (length < 0) throw new IllegalArgumentException();
		final long[] newBits;
		if (length == size) {
			newBits = bits.clone();
		} else if (length == 0) {
			newBits = new long[0];
		} else {
			final int from = position >> ADDRESS_BITS;
			final int to = (position + length + ADDRESS_MASK) >> ADDRESS_BITS;
			if ((position & ADDRESS_MASK) == 0) {
				newBits = Arrays.copyOfRange(bits, from, to);
			} else {
				final int s = position & ADDRESS_MASK;
				final int len = to - from;
				newBits = new long[len];
				//do all but last bit
				int j = from;
				int i = 0;
				for (; i < len - 1; i++, j++) {
					newBits[i] = (bits[j] >>> s) | (bits[j+1] << (ADDRESS_SIZE - s));
				}
				//do last bits as a special case
				if (j+1 < len) {
					newBits[i] = (bits[j] >>> s) | (bits[j+1] << (ADDRESS_SIZE - s));
				} else {
					newBits[i] = bits[j] >>> s;
				}
			}
		}
		return new BitVector(length, newBits);
	}
	
	// operations
	
	public void modify(Operation operation, boolean value) {
		perform(operation.ordinal(), value);
	}
	
	public void modifyBit(Operation operation, int position, boolean value) {
		perform(operation.ordinal(), position, value);
	}
	
	public void modifyBits(Operation operation, int position, long bits, int length) {
		perform(operation.ordinal(), position, bits, length);
	}
	
	public void modifyVector(Operation operation, BitVector vector) {
		perform(operation.ordinal(), vector);
	}
	
	public void modifyVector(Operation operation, int position, BitVector vector) {
		perform(operation.ordinal(), position, vector);
	}
	
	// convenience
	
	public void flip() {
		perform(XOR, true);
	}
	
	public void flipBit(int position) {
		perform(XOR, position, true);
	}
	
	public void set(boolean value) {
		perform(SET, value);
	}
	
	public void setBit(int position, boolean value) {
		perform(SET, position, value);
	}

	public void setByte(int position, byte value) {
		perform(SET, position, value, 8);
	}
	
	public void setShort(int position, short value) {
		perform(SET, position, value, 16);
	}
	
	public void setInt(int position, short value) {
		perform(SET, position, value, 32);
	}
	
	public void setLong(int position, short value) {
		perform(SET, position, value, 64);
	}
	
	public void setBits(int position, long value, int length) {
		perform(SET, position, value, length);
	}
	
	public void setVector(BitVector vector) {
		perform(SET, vector);
	}

	public void setVector(int position, BitVector vector) {
		perform(SET, position, vector);
	}

	public void and(boolean value) {
		perform(AND, value);
	}
	
	public void andBit(int position, boolean value) {
		perform(AND, position, value);
	}
	
	public void andByte(int position, byte value) {
		perform(AND, position, value, 8);
	}
	
	public void andShort(int position, short value) {
		perform(AND, position, value, 16);
	}
	
	public void andInt(int position, short value) {
		perform(AND, position, value, 32);
	}
	
	public void andLong(int position, short value) {
		perform(AND, position, value, 64);
	}
	
	public void andBits(int position, long value, int length) {
		perform(AND, position, value, length);
	}
	
	public void andVector(BitVector vector) {
		perform(AND, vector);
	}
	
	public void andVector(int position, BitVector vector) {
		perform(AND, position, vector);
	}

	public void or(boolean value) {
		perform(OR, value);
	}
	
	public void orBit(int position, boolean value) {
		perform(OR, position, value);
	}

	public void orByte(int position, byte value) {
		perform(OR, position, value, 8);
	}
	
	public void orShort(int position, short value) {
		perform(OR, position, value, 16);
	}
	
	public void orInt(int position, short value) {
		perform(OR, position, value, 32);
	}
	
	public void orLong(int position, short value) {
		perform(OR, position, value, 64);
	}
	
	public void orBits(int position, long value, int length) {
		perform(OR, position, value, length);
	}
	
	public void orVector(BitVector vector) {
		perform(OR, vector);
	}
	
	public void orVector(int position, BitVector vector) {
		perform(OR, position, vector);
	}

	public void xor(boolean value) {
		perform(XOR, value);
	}
	
	public void xorBit(int position, boolean value) {
		perform(XOR, position, value);
	}

	public void xorByte(int position, byte value) {
		perform(XOR, position, value, 8);
	}
	
	public void xorShort(int position, short value) {
		perform(XOR, position, value, 16);
	}
	
	public void xorInt(int position, short value) {
		perform(XOR, position, value, 32);
	}
	
	public void xorLong(int position, short value) {
		perform(XOR, position, value, 64);
	}
	
	public void xorBits(int position, long value, int length) {
		perform(XOR, position, value, length);
	}
	
	public void xorVector(BitVector vector) {
		perform(XOR, vector);
	}
	
	public void xorVector(int position, BitVector vector) {
		perform(XOR, position, vector);
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
		int h = size;
		for (int i = 0; i < bits.length; i++) {
			final long l = bits[i];
			h = h * 31 + ((int) l       );
			h = h * 31 + ((int)(l >> 32));
		}
		return h;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			sb.append(getBit(size - 1 - i) ? '1' : '0');
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
		final int i = position >> ADDRESS_BITS;
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
		switch (operation) {
		case SET :
			Arrays.fill(bits, m);
			break;
		case AND :
			for (int i = 0; i < bits.length; i++) bits[i] &= m;
			break;
		case XOR :
			for (int i = 0; i < bits.length; i++) bits[i] = ~bits[i];
			break;
		}
		if (value) bits[bits.length - 1] &= mask;
	}
	
	//assumes address size is size of long
	private void perform(int operation, int position, long bs, int length) {
		if (position < 0 || position + length > size) throw new IllegalArgumentException();
		if (length < 0 || length > ADDRESS_SIZE) throw new IllegalArgumentException();
		if (length == 0) return;
		final int i = position >> ADDRESS_BITS;
		final int s = position & ADDRESS_MASK;
		final long m = length == 64 ? -1L : (1L << length) - 1L;
		final long v = bs & m;
		if (s == 0) { // fast case, long-aligned
			bits[i] = bits[i] & ~m | v;
		} else if (s + length <= ADDRESS_SIZE) { //single long case
			bits[i] = bits[i] & Long.rotateLeft(~m, s) | (v << s);
		} else {
			bits[i]   = bits[i  ] & (-1L >>> (         ADDRESS_SIZE - s)) | (v <<                  s );
			bits[i+1] = bits[i+1] & (-1L <<  (length - ADDRESS_SIZE + s)) | (v >>> (ADDRESS_SIZE - s));
		}
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

	private void perform(int operation, int position, BitVector that) {
		if (this == null) throw new IllegalArgumentException("null vector");
		final int thatSize = that.size;
		if (thatSize == 0) return;
		final long[] thatBits = that.bits;
		//basic optimization: if the vector contains only one long perform the operation on the single long
		if (thatSize <= ADDRESS_SIZE) perform(operation, position, that.bits[0], thatSize);
		if (position < 0) throw new IllegalArgumentException("negative position");
		if (position + that.size > this.size) throw new IllegalArgumentException();
		final long[] bits = this.bits;
		final int from = position >> ADDRESS_BITS;
		final int to = (position + thatSize + ADDRESS_MASK) >> ADDRESS_BITS;
		final int s = position & ADDRESS_MASK;
		final int len = to - from;
		//skip first and last
		final int limit = len - 1;
		int j = from+1;
		int i = 1;
		if (s == 0) {
			switch(operation) {
			case SET :
				for (; i < limit; i++, j++) bits[j]  = thatBits[i];
				break;
			case AND :
				for (; i < limit; i++, j++) bits[j] &= thatBits[i];
				break;
			case OR :
				for (; i < limit; i++, j++) bits[j] |= thatBits[i];
				break;
			case XOR :
				for (; i < limit; i++, j++) bits[j] ^= thatBits[i];
				break;
			}
		} else {
			switch(operation) {
			case SET :
				for (; i < limit; i++, j++) bits[j]  = (thatBits[i] >>> s) | (thatBits[i+1] << (ADDRESS_SIZE - s));
				break;
			case AND :
				for (; i < limit; i++, j++) bits[j] &= (thatBits[i] >>> s) | (thatBits[i+1] << (ADDRESS_SIZE - s));
				break;
			case OR :
				for (; i < limit; i++, j++) bits[j] |= (thatBits[i] >>> s) | (thatBits[i+1] << (ADDRESS_SIZE - s));
				break;
			case XOR :
				for (; i < limit; i++, j++) bits[j] ^= (thatBits[i] >>> s) | (thatBits[i+1] << (ADDRESS_SIZE - s));
				break;
			}
		}
		//last bits as a special case
		//TODO work out how to mask these remaining bits
	}
	
}
