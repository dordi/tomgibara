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

/**
 * Records the range of values that a hash value may take. Both range values are inclusive.
 *  
 * @author tomgibara
 *
 */

//TODO really need to revisit the inclusivity of maximum bound
public class HashRange {

	// statics
	
	private final static BigInteger INT_MINIMUM = BigInteger.valueOf(Integer.MIN_VALUE);
	private final static BigInteger INT_MAXIMUM = BigInteger.valueOf(Integer.MAX_VALUE);
	private final static BigInteger LONG_MINIMUM = BigInteger.valueOf(Long.MIN_VALUE);
	private final static BigInteger LONG_MAXIMUM = BigInteger.valueOf(Long.MAX_VALUE);
	
	public static HashRange FULL_INT_RANGE = new HashRange(INT_MINIMUM, INT_MAXIMUM);
	public static HashRange FULL_LONG_RANGE = new HashRange(LONG_MINIMUM, LONG_MAXIMUM);
	
	// fields
	
	private final BigInteger minimum;
	private final BigInteger maximum;
	private BigInteger size = null;
	
	// constructors
	
	public HashRange(BigInteger minimum, BigInteger maximum) {
		if (minimum == null) throw new IllegalArgumentException();
		if (maximum == null) throw new IllegalArgumentException();
		if (minimum.compareTo(maximum) > 0) throw new IllegalArgumentException();
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public HashRange(int minimum, int maximum) {
		this(BigInteger.valueOf(minimum), BigInteger.valueOf(maximum));
	}
	
	public HashRange(long minimum, long maximum) {
		this(BigInteger.valueOf(minimum), BigInteger.valueOf(maximum));
	}
	
	// accessors
	
	public boolean isZeroBased() {
		return minimum.signum() == 0;
	}
	
	public boolean isIntRange() {
		return minimum.compareTo(INT_MINIMUM) >= 0 && maximum.compareTo(INT_MAXIMUM) <= 0;
	}
	
	public boolean isLongRange() {
		return minimum.compareTo(LONG_MINIMUM) >= 0 && maximum.compareTo(LONG_MAXIMUM) <= 0;
	}
	
	public BigInteger getMinimum() {
		return minimum;
	}
	
	public BigInteger getMaximum() {
		return maximum;
	}
	
	public BigInteger getSize() {
		return size == null ? size = maximum.subtract(minimum).add(BigInteger.ONE) : size;
	}
	
	public boolean isIntSize() {
		return getSize().compareTo(INT_MAXIMUM) <= 0;
	}
	
	public boolean isLongSize() {
		return getSize().compareTo(LONG_MAXIMUM) <= 0;
	}
	
	// methods
	
	public HashRange zeroBased() {
		return isZeroBased() ? this : new HashRange(BigInteger.ZERO, maximum.subtract(minimum));
	}
	
	// object methods
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof HashRange)) return false;
		HashRange that = (HashRange) obj;
		return this.minimum.equals(that.minimum) && this.maximum.equals(that.maximum);
	}
	
	@Override
	public int hashCode() {
		return minimum.hashCode() ^ 7 * maximum.hashCode();
	}
	
	@Override
	public String toString() {
		return "[" + minimum + ", " + maximum + "]";
	}
	
}
