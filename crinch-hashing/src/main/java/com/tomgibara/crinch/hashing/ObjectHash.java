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
 * Generic hash generator, produces a single hash which is the result of calling
 * {@link #hashCode()} on the object, or zero for null.
 * 
 * @author tomgibara
 * 
 */

public class ObjectHash<T> extends AbstractHash<T> {

	@Override
	public HashRange getRange() {
		return HashRange.FULL_INT_RANGE;
	}

	@Override
	public BigInteger hashAsBigInt(T value) {
		return BigInteger.valueOf(hashAsInt(value));
	}
	
	@Override
	public int hashAsInt(T value) {
		return value == null ? 0 : value.hashCode();
	}
	
	@Override
	public long hashAsLong(T value) {
		return hashAsInt(value);
	}
	
}
