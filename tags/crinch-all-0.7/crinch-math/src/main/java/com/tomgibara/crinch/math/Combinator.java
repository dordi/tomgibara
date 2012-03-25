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
package com.tomgibara.crinch.math;

import java.math.BigInteger;

/**
 * <p>
 * Provides indexed access to the set of all possible ways of choosing k items
 * from a set of n elements, disregarding order.
 * </p>
 * 
 * <p>
 * The elements of each combinations are numerically ordered. Furthermore, the
 * combinations themselves are indexed in strict numerical order, so that given
 * two combinations c1 and c2, that first differ at index i, c1[i] < c2[i]
 * implies c1 precedes c2.
 * </p>
 * 
 * <p>
 * Two combinators are equal if they generate the same sequence of combinations.
 * </p>
 * 
 * @author tomgibara
 * 
 */

public interface Combinator {

	/**
	 * The number of elements from which combinations are being formed.
	 * 
	 * @return the number of elements chosen from
	 */
	int getElementCount();
	
	/**
	 * The number of elements included in a combination
	 * 
	 * @return the number of elements chosen
	 */
	
	int getTupleLength();
	
	/**
	 * The number of combinations that can be formed
	 * 
	 * @return the number of combinations, at least one
	 */
	
	BigInteger size();
	
	/**
	 * Convenience method, calls {@link #getCombination(long, int[])} with a
	 * newly allocated array.
	 */
	
	int[] getCombination(long index) throws IndexOutOfBoundsException, IllegalArgumentException;

	/**
	 * Generates the combination at the specified index. This method is
	 * equivalent to calling {@link #getCombination(BigInteger, int[])} but may
	 * yield better performance.
	 * 
	 * @param index
	 *            the index of the combination to return
	 * @param array
	 *            an array whose length must be equal-to-or-greater-than the
	 *            tuple length
	 * @return the supplied array
	 * @throws IndexOutOfBoundsException
	 *             if the index is negative, or exceeds {@link #size()}-1
	 * @throws IllegalArgumentException
	 *             if the array is null or not large enough to accommodate the
	 *             combination
	 */
	int[] getCombination(long index, int[] array) throws IndexOutOfBoundsException, IllegalArgumentException;
	
	/**
	 * Convenience method, calls {@link #getCombination(BigInteger, int[])} with
	 * a newly allocated array.
	 */
	
	int[] getCombination(BigInteger index) throws IndexOutOfBoundsException, IllegalArgumentException;
	
	/**
	 * Generates the combination at the specified index.
	 * 
	 * @param index
	 *            the index of the combination to return
	 * @param array
	 *            an array whose length must be equal-to-or-greater-than the
	 *            tuple length
	 * @return the supplied array
	 * @throws IndexOutOfBoundsException
	 *             if the index is negative, or exceeds {@link #size()}-1
	 * @throws IllegalArgumentException
	 *             if the array is null or not large enough to accommodate the
	 *             combination
	 */
	int[] getCombination(BigInteger index, int[] array) throws IndexOutOfBoundsException, IllegalArgumentException;
	
}
