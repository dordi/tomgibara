package com.tomgibara.crinch.bits;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

//TODO use case for bloom filters needs immutability
//TODO need to watch for self-reference on mutations
public final class BitVector extends Number implements Cloneable, Iterable<Boolean> {

	// statics
	
	public enum Operation {
		SET,
		AND,
		OR,
		XOR
	}

	public enum Comparison {
		EQUALS,
		INTERSECTS,
		CONTAINS
	}
	
	private static final int SET = 0;
	private static final int AND = 1;
	private static final int OR  = 2;
	private static final int XOR = 3;

	private static final int EQUALS = 0;
	private static final int INTERSECTS = 1;
	private static final int CONTAINS = 2;
	
	private static final int ADDRESS_BITS = 6;
	private static final int ADDRESS_SIZE = 1 << ADDRESS_BITS;
	private static final int ADDRESS_MASK = ADDRESS_SIZE - 1;
	
	//necessary for throwing an IAE
	private static int stringLength(String str) {
		if (str == null) throw new IllegalArgumentException();
		return str.length();
	}
	
	// fields
	
	private final int start;
	private final int finish;
	private final long[] bits;
	private final long startMask;
	private final long finishMask;
	
	// constructors
	
	//creates a new bit vector of the specified size
	//naturally aligned
	public BitVector(int size) {
		if (size < 0) throw new IllegalArgumentException();
		if (size > (Integer.MAX_VALUE / 8)) throw new IllegalArgumentException();
		final int length = (size + ADDRESS_MASK) >> ADDRESS_BITS;
		this.bits = new long[length];
		this.start = 0;
		this.finish = size;
		this.startMask = -1L; 
		this.finishMask = -1L >>> (length * ADDRESS_SIZE - size) ;
	}
	
	//creates an aligned copy
	public BitVector(BitVector that) {
		final int size = that.finish - that.start;
		final int length = (size + ADDRESS_MASK) >> ADDRESS_BITS;
		this.start = 0;
		this.finish = size;
		this.bits = that.bits.clone();
		this.startMask = -1L;
		this.finishMask = -1L >>> (length * ADDRESS_SIZE - size);
	}
	
	//TODO consider allowing different radixes
	//creates a new bit vector from the supplied binary string
	//naturally aligned
	public BitVector(String str) {
		this(stringLength(str));
		//TODO can this be usefully optimized?
		for (int i = 0; i < finish; i++) {
			final char c = str.charAt(i);
			if (c == '1') setBit(finish - 1 - i, true);
			else if (c != '0') throw new IllegalArgumentException("Illegal character '" + c + "' at index " + i + ", expected '0' or '1'.");
		}
	}
	
	private BitVector(int start, int finish, long[] bits) {
		final int startIndex = start >> ADDRESS_BITS;
		final int finishIndex = (finish + ADDRESS_MASK) >> ADDRESS_BITS;
		this.start = start;
		this.finish = finish;
		this.bits = bits;
		final long startMask = -1 << (start - startIndex * ADDRESS_SIZE);
		final long finishMask = -1L >>> (finishIndex * ADDRESS_SIZE - finish);
		//TODO necessary? confirm this should be copied to all
		if (startIndex + 1 == finishIndex) {
			this.finishMask = this.startMask = startMask & finishMask;
		} else {
			this.startMask = startMask;
			this.finishMask = finishMask;
		}
	}
	
	// accessors
	
	public int size() {
		return finish - start;
	}

	public boolean isAligned() {
		return start == 0;
	}
	
	// getters
	
	public boolean getBit(int position) {
		if (position < 0) throw new IllegalArgumentException();
		position += start;
		if (position >= finish) throw new IllegalArgumentException();
		//can't assume inlining, so duplicate getBitImpl here
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
		if (position < 0) throw new IllegalArgumentException();
		if (length < 0) throw new IllegalArgumentException();
		position += start;
		if (position + length > finish) throw new IllegalArgumentException();
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
		if (position < 0) throw new IllegalArgumentException();
		if (length < 0) throw new IllegalArgumentException();
		position += start;
		if (position + length > finish) throw new IllegalArgumentException();
		final long[] newBits;
		if (length == finish) {
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
		return new BitVector(0, length, newBits);
	}
	
	// bit counting methods
	
	public int countOnes() {
		if (start == finish) return 0;
		if (start == 0) {
			int count = 0;
			for (int i = 0; i < bits.length; i++) {
				count += Long.bitCount(bits[i]);
			}
			return count;
		}
		return countOnes(0, finish);
	}

	public int countOnes(int from, int to) {
		if (from < 0) throw new IllegalArgumentException();
		if (from > to) throw new IllegalArgumentException();
		from += start;
		to += start;
		if (to > finish) throw new IllegalArgumentException();
		if (from == to) return 0;
		final int f = from >> ADDRESS_BITS;
		final int t = to >> ADDRESS_BITS;
		final int r = from & ADDRESS_MASK;
		final int l = to & ADDRESS_MASK;
		if (f == t) {
			final long m = (-1L >>> (ADDRESS_SIZE - l + r)) << r;
			return Long.bitCount(m & bits[f]);  
		}

		int count = 0;
		count += Long.bitCount( (-1L << r) & bits[f] );
		for (int i = f+1; i < t-1; i++) {
			count += Long.bitCount(bits[i]);
		}
		count += Long.bitCount( (-1L >>> (ADDRESS_SIZE - l)) & bits[t] );
		return count;
	}

	public int countZeros() {
		return finish - start - countOnes();
	}
	
	public int countZeros(int from, int to) {
		return to - from - countOnes(from, to);
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
	
	// comparisons
	
	public boolean compare(Comparison comparison, BitVector vector) {
		return compare(comparison.ordinal(), vector);
	}
	
	//TODO consider methods that compare subranges

	// views
	
	public byte[] toByteArray() {
		//TODO can optimize when byte aligned
		final int size = start - finish;
		final int length = (size + 7) >> 3;
		final byte[] bytes = new byte[length];
		if (length == 0) return bytes;
		if ((start & ADDRESS_MASK) == 0) { //long aligned case
			int i = start >> ADDRESS_BITS;
			int j = 0;
			for (; j < length - 7; i++) {
				final long l = bits[i];
				bytes[j++] = (byte) (  l        & 0xff);
				bytes[j++] = (byte) ( (l >>  8) & 0xff);
				bytes[j++] = (byte) ( (l >> 16) & 0xff);
				bytes[j++] = (byte) ( (l >> 24) & 0xff);
				bytes[j++] = (byte) ( (l >> 32) & 0xff);
				bytes[j++] = (byte) ( (l >> 40) & 0xff);
				bytes[j++] = (byte) ( (l >> 48) & 0xff);
				bytes[j++] = (byte) ( (l >> 56) & 0xff);
			}
			if (j < length) {
				final long l = bits[i];
				for (int k = 0; j < length; k++) {
					bytes[j++] = (byte) ( (l >> (k*8)) & 0xff);
				}
			}
		} else { //general case
			int i = 0;
			for (; i < length - 1; i++) {
				//TODO could use a getByteImpl?
				bytes[i] = getByte(i * 8);
			}
			bytes[i] = (byte) getBits(i * 8, size - i * 8);
		}
		return bytes;
	}
	
	// convenience setters
	
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

	// convenience comparisons
	
	public boolean equals(BitVector vector) {
		return compare(EQUALS, vector);
	}
	
	public boolean intersects(BitVector vector) {
		return compare(INTERSECTS, vector);
	}
	
	public boolean contains(BitVector vector) {
		return compare(CONTAINS, vector);
	}
	
	// number methods
	
	@Override
	public byte byteValue() {
		return getByte(0);
	}
	
	@Override
	public short shortValue() {
		return getShort(0);
	}
	
	@Override
	public int intValue() {
		return getInt(0);
	}
	
	@Override
	public long longValue() {
		return getLong(0);
	}
	
	public BigInteger bigIntValue() {
		return new BigInteger(toByteArray());
	}
	
	@Override
	public float floatValue() {
		//TODO can make more efficient by writing a method that returns vector in base 10 string
		return bigIntValue().floatValue();
	}
	
	@Override
	public double doubleValue() {
		//TODO can make more efficient by writing a method that returns vector in base 10 string
		return bigIntValue().doubleValue();
	}
	
	// iterable methods
	
	@Override
	public Iterator<Boolean> iterator() {
		return new BitIterator();
	}

	public ListIterator<Boolean> listIterator() {
		return new BitIterator();
	}

	public ListIterator<Boolean> listIterator(int index) {
		if (index < 0) throw new IllegalArgumentException();
		index += start;
		if (index > finish) throw new IllegalArgumentException();
		return new BitIterator(index);
	}
	
	// object methods
	
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BitVector)) return false;
		final BitVector that = (BitVector) obj;
		if (this.finish - this.start != that.finish - that.start) return false;
		return equals(that);
	}
	
	@Override
	public int hashCode() {
		final int size = finish - start;
		//trivial case
		if (size == 0) return size;
		int h = 0;
		//optimized case, starts at zero
		if (start == 0) {
			final int f = finish >> ADDRESS_BITS;
			for (int i = 0; i < f; i++) {
				final long l = bits[i];
				h = h * 31 + ((int) l       );
				h = h * 31 + ((int)(l >> 32));
			}
			if ((finish & ADDRESS_MASK) != 0) {
				final long l = bits[f] & finishMask;
				h = h * 31 + ((int) l       );
				h = h * 31 + ((int)(l >> 32));
			}
		} else {
			for (int i = 0; i < size; i += ADDRESS_SIZE) {
				//TODO consider a getBitsImpl?
				long l = getBits(i, 64);
				h = h * 31 + ((int) l       );
				h = h * 31 + ((int)(l >> 32));
			}
			final int r = size & ADDRESS_MASK;
			if (r != 0) {
				final long l = getBits(size - r, r);
				h = h * 31 + ((int) l       );
				h = h * 31 + ((int)(l >> 32));
			}
		}
		return h ^ size;
	}
	
	@Override
	public String toString() {
		final int size = finish - start;
		switch (size) {
		case 0 : return "";
		case 1 : return getBitImpl(0) ? "1" : "0";
		default :
			StringBuilder sb = new StringBuilder(size);
			for (int i = 0; i < size; i++) {
				sb.append(getBitImpl(size - 1 - i) ? '1' : '0');
			}
			return sb.toString();
		}
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
		if (position < 0)  throw new IllegalArgumentException();
		position += start;
		if (position >= finish) throw new IllegalArgumentException();
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
		if (position < 0) throw new IllegalArgumentException();
		if (length < 0 || length > ADDRESS_SIZE) throw new IllegalArgumentException();
		position += start;
		if (start + position + length > finish) throw new IllegalArgumentException();
		if (length == 0) return;
		final int i = position >> ADDRESS_BITS;
		final int s = position & ADDRESS_MASK;
		final long m = length == ADDRESS_SIZE ? -1L : (1L << length) - 1L;
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
		final int thisSize = this.finish - this.start;
		final int thatSize = that.finish - that.start;
		if (thisSize != thatSize) throw new IllegalArgumentException("incorrect size, expected " + thisSize + " and got " + thatSize);
		//TODO generalize to allow start to differ by 64 and finish to be unconstrained
		if ((this.start & ADDRESS_MASK) == 0 && (that.start & ADDRESS_MASK) == 0 && (this.finish & ADDRESS_MASK) == 0) {
			final int f = this.start >> ADDRESS_BITS;
			final int t = this.finish >> ADDRESS_BITS;
			final int d = (that.start - this.start) >> ADDRESS_BITS;
			long[] thisBits = this.bits;
			long[] thatBits = that.bits;
			switch (operation) {
			case SET :
				System.arraycopy(thatBits, f + d, thisBits, f, t - f);
				break;
			case AND :
				for (int i = f; i < t; i++) thisBits[i] &= thatBits[i + d];
				break;
			case OR:
				for (int i = f; i < t; i++) thisBits[i] |= thatBits[i + d];
				break;
			case XOR:
				for (int i = f; i < t; i++) thisBits[i] ^= thatBits[i + d];
				break;
			}
		} else {
			perform(operation, 0, that);
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
	
	private boolean compare(final int comp, final BitVector that) {
		if (this.finish - this.start != that.finish - that.start) throw new IllegalArgumentException();
		//trivial case
		if (this.start == this.finish) {
			switch (comp) {
			case EQUALS : return true;
			case INTERSECTS : return false;
			case CONTAINS : return true;
			default : throw new IllegalArgumentException("Unexpected comparison constant: " + comp);
			}
		}
		//fully optimal case - both start at 0
		//TODO can weaken this constraint - can optimize if their start are equal
		if (this.start == 0 && that.start == 0) {
			final long[] thisBits = this.bits;
			final long[] thatBits = that.bits;
			switch (comp) {
			case EQUALS :
				for (int i = thisBits.length-2; i >= 0; i++) {
					if (thisBits[i] != thatBits[i]) return false;
				}
				break;
			case INTERSECTS :
				for (int i = thisBits.length-2; i >= 0; i++) {
					if ((thisBits[i] & thatBits[i]) != 0) return true;
				}
				break;
			case CONTAINS :
				for (int i = thisBits.length-2; i >= 0; i++) {
					final long bits = thisBits[i];
					if ((bits | thatBits[i]) != bits) return false;
				}
				break;
			default : throw new IllegalArgumentException("Unexpected comparison constant: " + comp); 
			}
			{
				final long thisB = thisBits[thisBits.length - 1] & this.finishMask;
				final long thatB = thatBits[thatBits.length] & that.finishMask;
				switch (comp) {
				case EQUALS : return thisB == thatB;
				case INTERSECTS : return (thisB & thatB) == 0;
				case CONTAINS : return (thisB | thatB) == thisB;
				default : throw new IllegalArgumentException("Unexpected comparison constant: " + comp);
				}
			}
		}
		//TODO an additional optimization is possible when their starts differ by 64 
		//partially optimal case - both are address aligned
		if ((this.start & ADDRESS_MASK) == 0 && (that.start & ADDRESS_MASK) == 0 && (this.finish & ADDRESS_BITS) == 0) {
			final long[] thisBits = this.bits;
			final long[] thatBits = that.bits;
			final int f = this.start >> ADDRESS_BITS;
			final int t = this.finish >> ADDRESS_BITS;
			final int d = (that.start - this.start) >> ADDRESS_BITS;
			switch (comp) {
			case EQUALS :
				for (int i = f; i < t; i++) {
					if (thisBits[i] != thatBits[i+d]) return false;
				}
				return true;
			case INTERSECTS :
				for (int i = f; i < t; i++) {
					if ((thisBits[i] & thatBits[i+d]) != 0) return true;
				}
				return false;
			case CONTAINS :
				for (int i = f; i < t; i++) {
					final long bits = thisBits[i];
					if ((bits | thatBits[i+d]) != bits) return false;
				}
				return true;
			default : throw new IllegalArgumentException("Unexpected comparison constant: " + comp);
			}
		}
		//non-optimized case
		//TODO consider if this can be gainfully optimized
		final int size = finish - start;
		switch (comp) {
		case EQUALS :
			for (int i = 0; i < size; i++) {
				if (that.getBitImpl(i) != this.getBitImpl(i)) return false;
			}
			return true;
		case INTERSECTS :
			for (int i = 0; i < size; i++) {
				if (that.getBitImpl(i) && this.getBitImpl(i)) return true;
			}
			return false;
		case CONTAINS :
			for (int i = 0; i < size; i++) {
				if (that.getBitImpl(i) && !this.getBitImpl(i)) return false;
			}
			return true;
		default : throw new IllegalArgumentException("Unexpected comparison constant: " + comp);
		}
	}

	private boolean getBitImpl(int position) {
		position += start;
		final int i = position >> ADDRESS_BITS;
		final long m = 1L << (position & ADDRESS_MASK);
		return (bits[i] & m) != 0;

	}

	// inner classes
	
	private class BitIterator implements ListIterator<Boolean> {
		
		private final int from;
		private final int to;
		// points to the element that will be returned  by next
		private int index;

		BitIterator(int from, int to, int index) {
			this.from = from;
			this.to = to;
		}
		
		BitIterator(int index) {
			this(start, finish, index);
		}
		
		BitIterator() {
			this(start, finish, start);
		}
		
		@Override
		public boolean hasNext() {
			return index < to;
		}
		
		@Override
		public Boolean next() {
			if (!hasNext()) throw new NoSuchElementException();
			return Boolean.valueOf( getBitImpl(index++) );
		}
		
		@Override
		public int nextIndex() {
			return hasNext() ? index - start : -1;
		}
		
		@Override
		public boolean hasPrevious() {
			return index > from;
		}
		
		@Override
		public Boolean previous() {
			if (!hasPrevious()) throw new NoSuchElementException();
			return Boolean.valueOf( getBitImpl(--index) );
		}

		@Override
		public int previousIndex() {
			return hasPrevious() ? index - start - 1 : -1;
		}
		
		@Override
		public void set(Boolean bit) {
			setBit(index, bit);
		}
		
		@Override
		public void add(Boolean bit) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
}
