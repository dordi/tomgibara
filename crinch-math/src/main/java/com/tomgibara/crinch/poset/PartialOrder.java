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

public interface PartialOrder<E> {

	enum Comparison {

		EQUAL, // if a<=b and b<=a
		GREATER_THAN, // if b<=a but not a<=b
		LESS_THAN, // if a<=b but not b<=a
		INCOMPARABLE; //if neither a<=b or b<=a

		//TODO how to describe this ??
		public Comparison combine(Comparison that) {
			if (this == that) return this;
			if (this == INCOMPARABLE) return this;
			if (that == INCOMPARABLE) return that;
			if (this == EQUAL) return that;
			if (that == EQUAL) return this;
			return INCOMPARABLE;
		}
		
	}

	boolean isOrdered(E a, E b);
	
	Comparison compare(E a, E b);
	
}
