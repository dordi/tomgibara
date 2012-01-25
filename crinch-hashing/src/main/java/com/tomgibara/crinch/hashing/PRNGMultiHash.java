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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;

import com.tomgibara.crinch.util.ByteWriteStream;

/**
 * Generates an arbitrary length sequence of hash values using a
 * {@link SecureRandom}. Performance should not be expected to be good, but this
 * {@link MultiHash} implementation may be useful where general-purpose and/or
 * secure hash generation is required.
 * 
 * @author tomgibara
 * 
 * @param <T>
 *            the type of objects for which hashes will be generated
 */

public class PRNGMultiHash<T> extends AbstractMultiHash<T> {

	private static final HashRange sFullIntRange = new HashRange(BigInteger.ZERO, BigInteger.valueOf(Integer.MAX_VALUE));
	
	private final String algorithm;
	private final String provider;
	private final HashSource<T> source;
	private final HashRange range;
	private final boolean isFullIntRange;

	public PRNGMultiHash(HashSource<T> source, HashRange range) {
		this(null, null, source, range);
	}

	//max is inclusive
	public PRNGMultiHash(String algorithm, HashSource<T> source, HashRange range) {
		this(algorithm, null, source, range);
	}

	public PRNGMultiHash(String algorithm, String provider, HashSource<T> source, HashRange range) {
		if (source == null) throw new IllegalArgumentException("null source");
		if (range == null) throw new IllegalArgumentException("null range");

		this.algorithm = algorithm;
		this.provider = provider;
		this.source = source;
		this.range = range;
		
		isFullIntRange = sFullIntRange.equals(range);
		
		//verify algorithm exists
		if (algorithm != null) getRandom();
	}

	@Override
	public HashRange getRange() {
		return range;
	}
	
	@Override
	public int getMaxMultiplicity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int hashAsInt(T value) {
		if (!range.isIntBounded()) throw new IllegalStateException("not int bounded");
		if (isFullIntRange) {
			return getRandom(value).nextInt() & 0x7fffffff;
		} else {
			return range.getMinimum().intValue() + getRandom(value).nextInt(range.getSize().intValue());
		}
	}
	
	//TODO biased
	@Override
	public long hashAsLong(T value) {
		if (!range.isLongBounded()) throw new IllegalStateException("not long bounded");
		return range.getMinimum().longValue() + getRandom(value).nextLong() % range.getSize().longValue();
	}

	//TODO biased
	@Override
	public BigInteger hashAsBigInt(T value) {
		return hashAsBigInt(getRandom(value));
	}
	
	@Override
	public int[] hashAsInts(T value, int[] array) {
		if (!range.isIntBounded()) throw new IllegalStateException("not int bounded");
		final Random random = getRandom(value);
		if (isFullIntRange) {
			for (int i = 0; i < array.length; i++) {
				array[i] = random.nextInt() & 0x7fffffff;
			}
		} else {
			final int a = range.getMinimum().intValue();
			final int b = range.getSize().intValue();
			for (int i = 0; i < array.length; i++) {
				array[i] = a + random.nextInt(b);
			}
		}
		return array;
	}

	@Override
	public long[] hashAsLongs(T value, long[] array) {
		if (!range.isLongBounded()) throw new IllegalStateException("not int bounded");
		final Random random = getRandom(value);
		final long a = range.getMinimum().longValue();
		final long b = range.getSize().longValue();
		for (int i = 0; i < array.length; i++) {
			array[i] = a + random.nextLong() % b;
		}
		return array;
	}

	@Override
	public BigInteger[] hashAsBigInts(T value, BigInteger[] array) {
		final Random random = getRandom(value);
		for (int i = 0; i < array.length; i++) {
			array[i] = hashAsBigInt(random);
		}
		return array;
	}
	
	private BigInteger hashAsBigInt(Random random) {
		BigInteger size = range.getSize();
		int length = size.bitLength() + 7 / 8;
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return range.getMinimum().add( new BigInteger(1, bytes).mod(size) );
	}
	
	private Random getRandom(T value) {
		final Random random = getRandom();
		if (algorithm == null) {
			final CondensingWriteStream out = new CondensingWriteStream();
			source.sourceData(value, out);
			random.setSeed(out.getCondensedValue());
		} else {
			final ByteWriteStream out = new ByteWriteStream();
			source.sourceData(value, out);
			((SecureRandom)random).setSeed(out.getBytes());
		}
		return random;
	}

	private Random getRandom() {
		if (algorithm == null) {
			return new Random();
		} else {
			try {
				return provider == null ? SecureRandom.getInstance(algorithm) : SecureRandom.getInstance(algorithm, provider);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (NoSuchProviderException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
