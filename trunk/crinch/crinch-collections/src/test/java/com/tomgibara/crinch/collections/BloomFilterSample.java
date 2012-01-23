package com.tomgibara.crinch.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.ObjectHash;
import com.tomgibara.crinch.hashing.IntegerMultiHash;

public class BloomFilterSample {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		/**
		 * This introduction assumes familiarity with the general concept of
		 * Bloom filters. If you don't have this try reading:
		 * 
		 * http://en.wikipedia.org/wiki/Bloom_filter
		 */
		
		// HASHING
		
		/**
		 * Any Bloom filter requires a ready supply of hashes. In this
		 * implementation, hashing is a separate abstraction that allows for an
		 * unlimited variety of hash generating algorithms. Different
		 * applications will benefit from different algorithms, but to get
		 * started we can use this:
		 */

		ObjectHash<String> hash = new ObjectHash<String>();
		
		/**
		 * This simply exposes the String.hashCode() method as a Hash, so:
		 */

		assertEqual(hash.hashAsInt("Arnold"), "Arnold".hashCode());
		
		/**
		 * It also helpfully deals with null values by the simple expedient of
		 * hashing them to zero:
		 */
		
		assertEqual(hash.hashAsInt(null), 0);
		
		/**
		 * But for Bloom filters we generally need multiple hashes each entry.
		 * Objects that can generate more than one hash for a single argument
		 * implement the MultHash interface. There's a convenient implementation
		 * of this too:
		 */
		
		IntegerMultiHash<String> multiHash = new IntegerMultiHash<String>(hash, 99);
		
		/**
		 * That 99 in the constructor specifies a maximum value for the hash
		 * values it generates. This is an inclusive upper bound so we can be
		 * sure that:
		 */
		
		assertTrue(multiHash.hashAsInt("Kipper") < 100);
		assertTrue(multiHash.hashAsInt("Tiger") < 100);
		assertTrue(multiHash.hashAsInt("Arnold") < 100);
		
		/**
		 * This upper bound is important because it provides a convenient way to
		 * generate hashes that cover the number of bits in our Bloom filter.
		 */		
		
		// BLOOM FILTERS
		
		/**
		 * There's lots more useful code in the com.tomgibara.crinch.hashing package
		 * to deal with hashing, but we have all we need to create our Bloom filter:
		 */
		
		BloomFilter<String> filter = new BasicBloomFilter<String>(multiHash, 10);
		
		/**
		 * This will create a Bloom filter with a capacity of 100 (equal to the
		 * range of our MultiHash) that will set 10 bits for every String we add
		 * to it. Adding elements is easy:
		 */
		
		filter.add("Kipper");
		
		/**
		 * You can't remove elements from a Bloom filter, but you can clear it:
		 */
		
		filter.clear();
		
		/**
		 * Obviously, the filter is empty immediately after it's been cleared:
		 */
		
		assertTrue( filter.isEmpty() );
		
		/**
		 * Any time you add elements to a bloom filter, a boolean is returned that
		 * indicates whether the filter was modified as a result of the addition.
		 * This is analagous to the way the Java Collections API operates:
		 */
		
		assertTrue( filter.add("Kipper") );
		
		/**
		 * It's also possible to add multiple elements at once:
		 */

		List<String> kippersFriends = Arrays.asList("Tiger", "Pig");
		filter.addAll(kippersFriends);
		
		/**
		 * A Bloom filter is no use without the ability to test whether it
		 * contains particular values. This is possible using the mightContain()
		 * method:
		 */
		
		assertTrue( filter.mightContain("Kipper") );
		assertFalse( filter.mightContain("Arnold") );
		
		/**
		 * There's a convenient method for verifying the containment of
		 * collections of elements too:
		 */
		
		assertTrue( filter.mightContainAll(kippersFriends) );
		
		/**
		 * Of course, the word "might" appears in these methods because Bloom
		 * filters admit false positives, that is, they may claim to contain an
		 * element that was never actually added. So what is the probability of
		 * a false positive for our Bloom filter? It can estimate it for us:
		 */
		
		filter.getFalsePositiveProbability(); // tiny, about 1 in 10 million

		/**
		 * But this will increase as we add more elements:
		 */
		
		List<String> alphabet = Arrays.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));
		filter.addAll(alphabet); // we add another 26 entries
		filter.getFalsePositiveProbability(); // now not so tiny, about 1 in 2

		/**
		 * Statistics may lie, but probabilities don't; sure enough:
		 */
		
		assertTrue( filter.mightContain("Tom") );
		assertFalse( filter.mightContain("Dick") );
		assertFalse( filter.mightContain("Harry") );
		assertTrue( filter.mightContain("and all...") );
		
		// COMPATIBLE BLOOM FILTERS
		
		/**
		 * In addition to these basic operations, there are some extra things we
		 * can do when we have more than one Bloom filter:
		 */

		BloomFilter<String> otherFilter = new BasicBloomFilter<String>(multiHash, 10);

		/**
		 * For example we can check if they are equal:
		 */
		
		assertFalse( otherFilter.equals(filter) );
		
		/**
		 * To take things further we need the notion of two 'compatible' Bloom
		 * filters. Essentially two Bloom filters are compatible when they
		 * generate the same bit patterns. For implementations of BloomFilter
		 * this means that they have the same capacity, the same hash count and
		 * equal multi-hashes. Because our two Bloom filters are compatible we
		 * do this:
		 */		

		otherFilter.addAll(filter);
		
		/**
		 * This is generally an efficient bitwise operation and the only way of
		 * adding elements from one Bloom filter to another (since it's
		 * impossible to retrieve the elements that were added). We now find
		 * that:
		 */
		
		assertTrue( otherFilter.equals(filter) );
		
		/**
		 * Two Bloom filters are equal if they are compatible and have the same
		 * bit pattern. Between compatible Bloom filters, we also have a stronger
		 * containment test:
		 */		
		
		assertTrue( otherFilter.containsAll(filter) );
		assertTrue( filter.containsAll(otherFilter) );
		
		// BIT VECTORS
		
		/**
		 * All BloomFilter implementations are required to expose their bits
		 * via a BitVector:
		 */

		BitVector bits = filter.getBitVector();
		
		/**
		 * This is a very powerful class that can perform a multitude of bit
		 * related operations. Importantly, BitVectors are serializable:
		 */
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytes);
		out.writeObject(bits);
		out.close();
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
		BitVector otherBits = (BitVector) in.readObject();

		/**
		 * The persisted bits can be used to reconstruct an equal filter:
		 */
		
		otherFilter = new BasicBloomFilter<String>(otherBits.mutable(), multiHash, 10);
		assertEqual(otherFilter, filter);
		
		/**
		 * Care must be taken to ensure that all the other parameters of
		 * the BloomFilter remain consistent after deserialization, specifically
		 * the multi-hash and the hash count.
		 */
		
	}

	private static void assertEqual(Object x, Object y) {
		if (!x.equals(y)) throw new IllegalStateException();
	}

	private static void assertTrue(boolean b) {
		if (!b) throw new IllegalStateException();
	}

	private static void assertFalse(boolean b) {
		if (b) throw new IllegalStateException();
	}

}
