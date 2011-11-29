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
package com.tomgibara.crinch.math;

abstract class AbstractCombinator implements Combinator {

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Combinator)) return false;
		Combinator that = (Combinator) obj;
		if (this.getElementCount() != that.getElementCount()) return false;
		if (this.getTupleLength() != that.getTupleLength()) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return getElementCount() ^ (getTupleLength() * 31);
	}
	
	@Override
	public String toString() {
		return getElementCount() + " choose " + getTupleLength();
	}
}
