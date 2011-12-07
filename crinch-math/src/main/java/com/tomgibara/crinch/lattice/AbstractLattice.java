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
package com.tomgibara.crinch.lattice;

import com.tomgibara.crinch.poset.AbstractPartialOrder;

public abstract class AbstractLattice<E> extends AbstractPartialOrder<E> implements Lattice<E> {

	@Override
	public Lattice<E> bounded(E top, E bottom) {
		return boundedAbove(top).boundedBelow(bottom);
	}

	@Override
	public boolean equalInLattice(E e1, E e2) {
		if (!contains(e1) || !contains(e2)) throw new IllegalArgumentException();
		E m = meet(e1, e2);
		E j = join(e1, e2);
		return m.equals(j);
	}

	@Override
	public boolean isBounded() {
		return isBoundedBelow() && isBoundedAbove();
	}

	@Override
	public boolean isOrdered(E e1, E e2) {
		if (!contains(e1) || !contains(e2)) throw new IllegalArgumentException();
		return join(e1, e2).equals(e2);
	}

}
