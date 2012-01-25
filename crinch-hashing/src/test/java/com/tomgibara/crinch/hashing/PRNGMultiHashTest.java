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

import junit.framework.TestCase;

import com.tomgibara.crinch.util.WriteStream;

public class PRNGMultiHashTest extends TestCase {

	public void testBasic() throws Exception {
		
		HashSource<Integer> s = new HashSource<Integer>() {
			
			@Override
			public void sourceData(Integer value, WriteStream out) {
				out.writeInt(value);
			}
		};
		
		PRNGMultiHash<Integer> mh = new PRNGMultiHash<Integer>("SHA1PRNG", s, new HashRange(0, 50));
		final HashRange r = mh.getRange();
		final int m = 10;
		for (int i = 0; i < 100; i++) {
			int[] hashes = mh.hashAsInts(i, m);
			assertTrue(hashes.length == m);
			for (int j = 0; j < m; j++) {
				final int h = hashes[j];
				assertTrue(BigInteger.valueOf(h).compareTo(r.getMaximum()) <= 0);
				assertTrue(r.getMinimum().compareTo(BigInteger.valueOf(h)) <= 0);
			}
			//TODO need to execute a statistical test for uniform distribution here
		}
		
	}
	
}
