package com.tomgibara.crinch.hashing;

/**
 * Extends the {@link Hash} to allow implementations to return multiple hash
 * values for a single object. This is useful in data structures that require
 * multiple hash values for their operation (eg. Bloom filters). It is
 * anticipated, though not required, that the hash values returned through the
 * methods defined on {@link Hash} will be those at the first index in the
 * {@link HashList} returned by {@link #hashAsList(Object, int)}.
 * 
 * @author tomgibara
 * 
 * @param <T>
 */
public interface MultiHash<T> extends Hash<T> {

	/**
	 * The greatest number of hash values that this object can create for a
	 * single object of type T. If there is no limit to the number of hashes
	 * that might be generated Integer.MAX_VALUE should be returned.
	 * 
	 * @return the maximum size of {@link HashList} that can be requested from
	 *         the {@link #hashAsList(Object, int)} method
	 */
	//TODO find a better name for this
    int getMaxMultiplicity();

	/**
	 * Creates a number of hash values for a single object. If the specified
	 * multiplicity does not exceed the value returned by
	 * {@link #getMaxMultiplicity()} then the returned list is guaranteed to
	 * contain at least as many hash values.
	 * 
	 * @param value
	 *            the object to be hashed
	 * @param multiplicity
	 *            the minimum number of hash values that must be returned
	 * @return a list containing one or more hash values.
	 * @throws IllegalArgumentException
	 *             if the multiplicity exceeds the value returned by
	 *             {@link #getMaxMultiplicity()}, or if the value could not be
	 *             hashed
	 */

    HashList hashAsList(T value, int multiplicity)throws IllegalArgumentException;

}
