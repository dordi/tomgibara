package com.tomgibara.crinch.math;

/**
 * A {@link Combinator} over a constrained set of parameters that can avoid any
 * array allocations by packing the elements of a combination into a single
 * long.
 * 
 * @author tom
 * 
 */

public interface PackedCombinator extends Combinator {

	/**
	 * The number of bits into which each combination element is packed.
	 * 
	 * @return the bits per element
	 */
	
	int getBitsPerElement();
	
	/**
	 * <p>
	 * Generates the combination at the specified index. This method can provide
	 * significantly better performance than those which operate with arrays.
	 * </p>
	 * 
	 * <p>Bits are packed into the LSB of the returned long</p>
	 * 
	 * @param index
	 *            the index of the combination to return
	 * @return the combined elements packed into a long
	 * @throws IndexOutOfBoundsException
	 *             if the index is negative, or exceeds {@link #size()}-1
	 */
	long getPackedCombination(long index) throws IndexOutOfBoundsException;

	/**
	 * Unpacks a single element from a value returned by
	 * {@link #getPackedCombination(long)}.
	 * 
	 * @param packed
	 *            a tuple of elements packed into a long
	 * @param i
	 *            the index of the element in the tuple
	 * @return the element at the specified position
	 * @throws IllegalArgumentException
	 *             if i is negative or exceeds the tuple length
	 */
	
	int getPackedElement(long packed, int i) throws IllegalArgumentException;
	
}
