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
