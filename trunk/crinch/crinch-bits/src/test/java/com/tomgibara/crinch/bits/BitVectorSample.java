package com.tomgibara.crinch.bits;

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
			 * are affected. Conversely, all the targeted bits must lay within
			 * the span of the vector. Operations can apply bits from a number
			 * of other sources, some of which will be described later.
			 */

			/**
			 * The NOT operation is also supported, but is referred to as "flip"
			 * for consistency with other classes in the standard Java
			 * libraries.
			 */

			v.flipBit(2);
			assertEquals(new BitVector("0000010101"), v);
			v.flipBit(2);
			assertEquals(new BitVector("0000010001"), v);

			/**
			 * For convenience, it's easy to target every bit in a single operation. For
			 * example, this will clear all bits:
			 */

			v.set(false);
			assertEquals(new BitVector("0000000000"), v);
			
			/**
			 * and this will flip them all:
			 */
			
			v.flip();
			assertEquals(new BitVector("1111111111"), v);
		
			//TODO modify versions
		}

		{ // SHIFTS AND ROTATIONS
			// shift, rotate, reverse
		}
		
		{ // NUMBERS
			//BigInteger constructor
			//xxValue methods
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
			//iterator, listIterator, positionIterator
			//asList
			//asSet
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
