/*
 * Copyright 2011 Tom Gibara
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
package com.tomgibara.crinch.perm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermutationSequenceTest extends PermutationTestCase {

	public void testNextAndPreviousByOrder() {
		int count = 1;
		for (int size = 0; size < 5; size++) {
			if (size > 0) count *= size;
			Permutation.Generator pg = Permutation.identity(size).generator();
			PermutationSequence ps = pg.getOrderedSequence();
			Permutation p;
			
			Set<Permutation> set1 = new HashSet<Permutation>();
			List<Permutation> list1 = new ArrayList<Permutation>();
			p = pg.permutation();
			set1.add(p);
			list1.add(p);
			while (ps.hasNext()) {
				p = ps.next().getGenerator().permutation();
				set1.add(p);
				list1.add(p);
			} 
			assertEquals(count, set1.size());
			assertEquals(count, list1.size());
			
			Set<Permutation> set2 = new HashSet<Permutation>();
			List<Permutation> list2 = new ArrayList<Permutation>();
			p = pg.permutation();
			set2.add(p);
			list2.add(p);
			while (ps.hasPrevious()) {
				p = ps.previous().getGenerator().permutation();
				set2.add(p);
				list2.add(p);
			}
			assertEquals(count, set2.size());
			assertEquals(count, list2.size());
			
			assertEquals(set1, set2);
			Collections.reverse(list2);
			assertEquals(list1, list2);
		}
	}

}
