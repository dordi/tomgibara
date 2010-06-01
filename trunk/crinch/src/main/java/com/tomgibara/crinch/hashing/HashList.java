package com.tomgibara.crinch.hashing;

import java.math.BigInteger;
import java.util.List;

/**
 * An interface used to expose multiple hash values from a {@link MultiHash}.
 * 
 * @author tomgibara
 *
 */

public interface HashList extends List<BigInteger> {

	
	/**
	 * The hash value at the specified index as a {@link BigInteger}.
	 * 
	 * @param index
	 *            the index of the hash value
	 * @return the hash value at the given index
	 */

	@Override
	BigInteger get(int index);

	/**
	 * The hash value at the specified index as an int. This method should
	 * provide better performance for integer-ranged hashes. This value is not
	 * guaranteed to lie within the indicated {@link HashRange} unless
	 * {@link HashRange#isIntRange()} is true.
	 * 
	 * @param index
	 *            the index of the hash value
	 * @return the hash value at the given index
	 */

    int getAsInt(int i) throws IndexOutOfBoundsException;
    
	/**
	 * The hash value at the specified index as a long. This method should
	 * provide better performance for long-ranged hashes. This value is not
	 * guaranteed to lie within the indicated {@link HashRange} unless
	 * {@link HashRange#isLongRange()} is true.
	 * 
	 * @param index
	 *            the index of the hash value
	 * @return the hash value at the given index
	 */

    long getAsLong(int i) throws IndexOutOfBoundsException;
	
}
