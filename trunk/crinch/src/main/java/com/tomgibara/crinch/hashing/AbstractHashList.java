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
import java.util.AbstractList;

/**
 * Convenience base class for implementing the {@link HashList} interface. At a
 * minimum, one of {@link #get(int)} or {@link #getAsLong(int)} must be
 * implemented in addition to the {@link #size()} method.
 * 
 * @author tomgibara
 * 
 */

public abstract class AbstractHashList extends AbstractList<BigInteger> implements HashList {

	@Override
	public BigInteger get(int index) {
		return BigInteger.valueOf(getAsLong(index));
	}

	@Override
	public int getAsInt(int index) throws IndexOutOfBoundsException {
		return get(index).intValue();
	}

	@Override
	public long getAsLong(int index) throws IndexOutOfBoundsException {
		return get(index).longValue();
	}

}
