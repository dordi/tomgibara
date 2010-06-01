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
 * Convenience base class for implementing the {@link Hash} interface. At a
 * minimum, one of {@link #hashAsBigInt(Object)} or {@link #hashAsLong(Object)}
 * needs to be implemented in addition to the {@link #getRange()} method.
 * 
 * @author tomgibara
 * 
 * @param <T> the type of object over which hashes may be generated
 */

public abstract class AbstractHash<T> implements Hash<T> {

	@Override
	public BigInteger hashAsBigInt(T value) {
		return BigInteger.valueOf(hashAsLong(value));
	}

	@Override
	public int hashAsInt(T value) {
		return hashAsBigInt(value).intValue();
	}

	@Override
	public long hashAsLong(T value) {
		return hashAsBigInt(value).longValue();
	}

}
