/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

class RerangedHash<T> extends AdaptedMultiHash<T> {

	final HashRange oldRange;
	final HashRange newRange;
	final boolean isSmaller;

	final BigInteger bigOldMin;
	final BigInteger bigNewMin;
	final BigInteger bigNewSize;


	RerangedHash(MultiHash<T> hash, HashRange newRange) {
		super(hash);
		this.newRange = newRange;
		oldRange = hash.getRange();
		isSmaller = newRange.getSize().compareTo(oldRange.getSize()) < 0;
		
		bigOldMin = oldRange.getMinimum();
		bigNewMin = newRange.getMinimum();
		bigNewSize = newRange.getSize();
	}
	
	@Override
	public HashRange getRange() {
		return newRange;
	}

}
