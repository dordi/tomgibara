/*
 * Copyright 2010 Tom Gibara
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
package com.tomgibara.crinch.poset;

public abstract class AbstractPartialOrder<E> implements PartialOrder<E> {

	@Override
	public Comparison compare(E a, E b) {
		final boolean alteb = isOrdered(a, b);
		final boolean bltea = isOrdered(b, a);
		if (alteb) {
			return bltea ? Comparison.EQUAL : Comparison.LESS_THAN;
		} else {
			return bltea ? Comparison.GREATER_THAN : Comparison.INCOMPARABLE;
		}
	}
	
}
