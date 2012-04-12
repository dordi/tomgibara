/*
 * Copyright 2010 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.hashing;

import java.math.BigInteger;
import java.util.List;

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

    int[] hashAsInts(T value, int multiplicity) throws IllegalArgumentException;
    
    long[] hashAsLongs(T value, int multiplicity) throws IllegalArgumentException;

    BigInteger[] hashAsBigInts(T value, int multiplicity) throws IllegalArgumentException;

    int[] hashAsInts(T value, int[] array) throws IllegalArgumentException;
    
    long[] hashAsLongs(T value, long[] array) throws IllegalArgumentException;

    BigInteger[] hashAsBigInts(T value, BigInteger[] array) throws IllegalArgumentException;

}
