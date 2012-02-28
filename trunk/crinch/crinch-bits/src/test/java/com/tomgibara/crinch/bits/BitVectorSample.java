package com.tomgibara.crinch.bits;

import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitVector.Operation;

import junit.framework.TestCase;

public class BitVectorSample extends TestCase {

	public void testSample() throws Exception {

		// INTRODUCTION

		/**
		 * A BitVector is a fixed-length sequence of bits. It's an extremely
		 * powerful class because of the huge number of ways that it allows the
		 * bits to be accessed and modified. Algorithms that rely heavily on bit
		 * manipulation, may improve their performance by reducing the frequency
		 * with which bit data needs to be moved between different data
		 * structures; they can rely on BitVector for all the bit manipulations
		 * they require.
		 */

		{ // SIMPLE BITS

			/**
			 * There are many perspectives from which a BitVector can be viewed.
			 * The simplest is to see it as an indexed sequence of bits with a
			 * fixed size. This creates a BitVector of size 10:
			 */

			BitVector v = new BitVector(10);

			/**
			 * It's fine to have an empty BitVector:
			 */

			BitVector empty = new BitVector(0);

			/**
			 * The size of a bit vector is fixed and accessible.
			 */

			assertEquals(10, v.size());
			assertEquals(0, empty.size());

			/**
			 * Initially all of the bits are zero. The BitVector class
			 * universally represents bits as booleans (true/false) rather than
			 * (0/1) since it naturally provides a much better API. So to set
			 * the first bit of a BitVector we can call:
			 */

			v.setBit(0, true);

			/**
			 * As this makes clear (if it were necessary), the bits are zero
			 * indexed. In addition to being zero indexed, bits are arranged
			 * 'big-endian' for consistency with the Java language. Normally
			 * this is of no concern, since the internal representation of the
			 * bits is rarely a concern when using the class. One place where it
			 * may be seem confusing at first is the toString() method:
			 */

			assertEquals("0000000001", v.toString());

			/**
			 * If thinking of BitVector as a straightforward list one might
			 * expect the first bit to appear on the left. Instead, it's best to
			 * regard the zeroth bit of a BitVector as being the least
			 * significant bit - and in binary representation that appears on
			 * the right.
			 * 
			 * It's also possible to construct a BitVector directly from a
			 * string. This is analogous to BigInteger's String constructor.
			 */

			assertEquals(new BitVector("0000000001"), v);

			/**
			 * Here's just one of the basic bit level operations that is
			 * available:
			 */

			v.orBit(1, true);
			assertEquals(new BitVector("0000000011"), v);

			/**
			 * In addition to OR, AND and XOR are also available. Any operation
			 * can be applied to a range rather than a single bit:
			 */

			v.xorRange(1, 5, true);
			assertEquals(new BitVector("0000011101"), v);

			/**
			 * Observe that, as is the norm in Java, the lower bound is
			 * inclusive and the upper bound is exclusive. Additionally, Any
			 * operation can be applied using the bits from a byte, int or long.
			 * Here we AND the binary representation of 9 ending at the 1st bit:
			 */

			v.andByte(1, (byte) 9);
			assertEquals(new BitVector("0000010001"), v);

			/**
			 * This is subtle, but nevertheless powerful: all of the eight bits
			 * spanned by the byte have been ANDed and none of the other bits
			 * are modified. Conversely, all the targeted bits must lay within
			 * the span of the vector. So what if you have a number but you only
			 * want to apply some of its bits? For this, the number of (least
			 * significant bits) that should be modified can specified
			 */

			v.setBits(6, 12, 4); // apply 4 bits starting at index 6
			assertEquals(new BitVector("1100010001"), v);

			/**
			 * The NOT operation is also supported, but is referred to as "flip"
			 * for consistency with other classes in the standard Java
			 * libraries.
			 */

			v.flipBit(2);
			assertEquals(new BitVector("1100010101"), v);
			v.flipBit(2);
			assertEquals(new BitVector("1100010001"), v);

			/**
			 * For convenience, it's easy to target every bit in a single
			 * operation. For example, this will clear all bits:
			 */

			v.set(false);
			assertEquals(new BitVector("0000000000"), v);

			/**
			 * and this will flip them all:
			 */

			v.flip();
			assertEquals(new BitVector("1111111111"), v);

			/**
			 * For every method that applies a bit operation, there is a
			 * corresponding method which does the same thing, but takes the
			 * operator as an additional parameter. The following pairs of calls
			 * are equivalent:
			 */

			v.andBit(0, true);
			v.modifyBit(Operation.AND, 0, true);

			v.setRange(8, 10, true);
			v.modifyRange(Operation.SET, 8, 10, true);

		}

		{ // NUMBERS

			/**
			 * Just as Java's integral primitives are fixed-width bit words, a
			 * BitVector can be regarded as an integral value, but in contrast
			 * to Java, BitVector values are always unsigned. It's easy to
			 * create a BitVector from a number using the string constructor
			 * which takes a radix.
			 */

			String monsterDigits = "808017424794512875886459904961710757005754368000000000";
			BitVector v = new BitVector(monsterDigits, 10);

			/**
			 * BitVectors can also be constructed from BigIntegers.
			 */

			BigInteger monsterInteger = new BigInteger(monsterDigits);
			BitVector w = BitVector.fromBigInteger(monsterInteger);

			assertEquals(v, w);

			/**
			 * BitVectors constructed in this way will always contain the least
			 * number of bits required to contain the number; in other words,
			 * the most significant bit is always 1.
			 */

			assertTrue(v.getBit(v.size() - 1));

			/**
			 * Another consequence is that a BitVector constructed from the
			 * number zero will have a size of zero.
			 */

			assertEquals(0, new BitVector("0", 10).size());
			assertEquals(0, BitVector.fromBigInteger(BigInteger.ZERO).size());

			/**
			 * If a greater number of bits is required, the size of a BitVector
			 * constructed from a BigInteger can be supplied as an extra
			 * parameter:
			 */

			assertEquals(new BitVector("00000001"), BitVector.fromBigInteger(
					BigInteger.ONE, 8));

			/**
			 * Note that, because BitVectors are unsigned, any attempt to
			 * construct one from a negative number will fail. So far we've
			 * dealt with converting numbers into BitVectors, of course, it's
			 * possible to do the reverse and convert a BitVector back into a
			 * number:
			 */

			assertEquals(monsterInteger, v.toBigInteger());

			/**
			 * Reflecting BitVector's role in storing numeric values, the class
			 * implements the Number interface and any BitVector can be
			 * converted into primitive values via that interface.
			 */

			assertEquals(monsterInteger.longValue(), v.longValue());

			assertEquals(234, new BitVector("234", 10).intValue());

			/**
			 * As with all other implementations of Number in the core Java
			 * packages, these methods take the least significant bits of the
			 * BitVector and silently truncate any other bits.
			 */

			assertEquals(85, new BitVector("0101010101010101").byteValue());

			/**
			 * Note that truncation can result in negative values being returned
			 * from these methods even though the BitVector itself always
			 * represents a positive whole number.
			 */

			assertTrue(monsterInteger.longValue() < 0L);

			/**
			 * It is also possible to extract bits from any position in the
			 * BitVector as a primitive value.
			 */

			assertEquals((byte) (v.longValue() >>> 56), v.getByte(56));

		}

		{ // SHIFTS AND ROTATIONS

			/**
			 * Just as Java provides bit shift operators for ints and longs,
			 * BitVector provides the same functionality (and more) but for
			 * potentially much larger bit sequences. BitVector provides a
			 * single method that handles all of Java's bit shift operators
			 * (>>>, >> and <<). It takes two parameters, the first indicates
			 * how far the bits should be shifted with the sign of the number
			 * indicating the direction (negative is left, positive is right)
			 * and the second gives the value that should used to populate the
			 * vacated bits.
			 */

			BitVector v = new BitVector("11001010");
			v.shift(1, true);
			assertEquals(new BitVector("10010101"), v);
			v.shift(-2, false);
			assertEquals(new BitVector("00100101"), v);

			/**
			 * There is no limit to the distance that a BitVector may be
			 * shifted. Obviously any shift distance that exceeds the size of
			 * the BitVector will eradicate all bits.
			 */

			v.shift(8, false);
			assertEquals(new BitVector("00000000"), v);

			/**
			 * Similar to bit shifts, BitVector can also rotate bits. Bits that
			 * are pushed off one end populate the vacated positions at the
			 * other.
			 */

			v = new BitVector("11001010");
			v.rotate(1);
			assertEquals(new BitVector("10010101"), v);

			/**
			 * As with shifting, there is no limit to the distance over which
			 * bits can be rotated. Unlike shifting, bits are never lost.
			 */

			v = new BitVector("11001010");
			v.rotate(v.size());
			assertEquals(new BitVector("11001010"), v);

			/**
			 * In addition to shifts and rotations, BitVector can reverse the
			 * order of the bits.
			 */

			v.reverse();
			assertEquals(new BitVector("01010011"), v);
			
			/**
			 * Reversing a BitVector twice will naturally restore the bits to
			 * their original order.
			 */

			v.reverse();
			assertEquals(new BitVector("11001010"), v);

			/**
			 * Finally, you should be aware that all of these operations
			 * (shifting, rotating and reversing) are available over arbitrary
			 * ranges. Here are some examples:
			 */

			v = new BitVector("11001010");
			v.shiftRange(0, 4, 1, false);
			assertEquals(new BitVector("11000100"), v);
			v.rotateRange(2, 7, -1);
			assertEquals(new BitVector("11100000"), v);
			v.reverseRange(2, 8);
			assertEquals(new BitVector("00011100"), v);
			
		}

		{ // COPIES AND VIEWS

		}

		{ // VECTOR OPERATIONS
			// compare, testX
		}

		{ // ANALYZING
			// first, last, count, all, next
		}

		{ // BYTES
			// byte static factory method
			// operations on byte arrays
			// write/read methods
		}

		{ // COLLECTIONS
			// iterator, listIterator, positionIterator
			// asList
			// asSet
		}

		{ // JAVA OBJECT
			// cloneable
			// serializable
			// equals, hashcode
		}

		{ // EFFICIENCY
			// alignment
			// avoiding no-ops
			// getThen... methods
		}
	}
}