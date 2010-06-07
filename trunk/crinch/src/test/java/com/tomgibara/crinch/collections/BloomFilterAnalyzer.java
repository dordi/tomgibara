package com.tomgibara.crinch.collections;

import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.hashing.ObjectHashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;

public class BloomFilterAnalyzer {

	public static void main(String[] args) {

		//don't bother with any validation yet
		final int size = Integer.parseInt(args[0]); // size of bloom filter
		final int count = Integer.parseInt(args[1]); // number of elements to test over
		final int max = Integer.parseInt(args[2]); // max number of elements to insert
		final int step = Integer.parseInt(args[3]); // step size from zero to max
		final int k = Integer.parseInt(args[4]); // number of hashes to use
		final boolean useSha1 = Boolean.parseBoolean(args[5]);
		
		//use a sha1 hash because it should provide very good hashing
		final MultiHash<Object> multiHash = useSha1 ?
			new PRNGMultiHash<Object>("SHA1PRNG", new ObjectHashSource(), size - 1) :
			new PRNGMultiHash<Object>(new ObjectHashSource(), size - 1);
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
