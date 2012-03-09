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
package com.tomgibara.crinch.collections;

import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.hashing.ObjectHash;
import com.tomgibara.crinch.hashing.ObjectHashSource;
import com.tomgibara.crinch.hashing.IntegerMultiHash;
import com.tomgibara.crinch.hashing.PRNGMultiHash;

public class BloomFilterAnalyzer {

	public static void main(String[] args) {

		//don't bother with any validation yet
		final int size = Integer.parseInt(args[0]); // size of bloom filter
		final int count = Integer.parseInt(args[1]); // number of elements to test over
		final int max = Integer.parseInt(args[2]); // max number of elements to insert
		final int step = Integer.parseInt(args[3]); // step size from zero to max
		final int k = Integer.parseInt(args[4]); // number of hashes to use
		final String algo = args[5].intern(); // the algorithms to use
		
		final MultiHash<Object> multiHash;
		if (algo == "sha1") {
			multiHash = new PRNGMultiHash<Object>("SHA1PRNG", new ObjectHashSource(), new HashRange(0, size - 1));
		} else if (algo == "rnd") {
			multiHash = new PRNGMultiHash<Object>(new ObjectHashSource(), new HashRange(0, size - 1));
		} else if (algo == "obj") {
			multiHash = new IntegerMultiHash<Object>(new ObjectHash<Object>(), size - 1);
		} else {
			throw new IllegalArgumentException(algo);
		}
		final BasicBloomFilter<Integer> bf = new BasicBloomFilter<Integer>(multiHash, k);
		
		final long startTime = System.currentTimeMillis();
		for (int i = 0; i < max; i += step) {
			//clear ready for this run
			bf.clear();
			//add i elements
			for (int j = 0; j < i; j++) bf.add(j);
			//count false positives
			int fpCount = 0;
			for (int j = i; j < count; j++) if (bf.mightContain(j)) fpCount++;
			//calculate the false positive prob.
			double fpp = (double)fpCount / (count-i);
			//display result
			System.out.println( String.format("%d\t%1.6f", i, fpp) );
		}
		final long finishTime = System.currentTimeMillis();
		System.out.println("(Time taken " + (finishTime - startTime) + "ms)");
	}
	
}
