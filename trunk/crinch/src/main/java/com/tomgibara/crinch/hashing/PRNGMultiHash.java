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

public class PRNGMultiHash<T> implements MultiHash<T> {

	private final String algorithm;
	private final String provider;
	private final HashSource<T> source;
	private final int max;
	private final HashRange range;

	//max is inclusive
	public PRNGMultiHash(String algorithm, HashSource<T> source, int max) {
		this(algorithm, null, source, max);
	}

	public PRNGMultiHash(String algorithm, String provider, HashSource<T> source, int max) {
		if (algorithm == null) throw new IllegalArgumentException("null algorithm");
		if (source == null) throw new IllegalArgumentException("null source");
		if (max < 0) throw new IllegalArgumentException("negative max");

		this.algorithm = algorithm;
		this.provider = provider;
		this.source = source;
		this.max = max;
		this.range = new HashRange(BigInteger.ZERO,  BigInteger.valueOf(max) );
		//verify algorithm exists
		getRandom();
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
	public BigInteger hashAsBigInt(T value) {
		return BigInteger.valueOf(hashAsInt(value));
	}
	
	@Override
	public int hashAsInt(T value) {
		if (max == Integer.MAX_VALUE) {
			return getRandom(value).nextInt() & 0x7fffffff;
		} else {
			return getRandom(value).nextInt(max + 1);
		}
	}
	
	@Override
	public long hashAsLong(T value) {
		return hashAsInt(value);
	}
	
	@Override
	public HashList hashAsList(final T value, final int multiplicity) {
		final int[] hashes = new int[multiplicity];
		final SecureRandom random = getRandom(value);
		if (max == Integer.MAX_VALUE) {
			for (int i = 0; i < hashes.length; i++) {
				hashes[i] = random.nextInt() & 0x7fffffff;
			}
		} else {
			final int m = max;
			for (int i = 0; i < hashes.length; i++) {
				hashes[i] = random.nextInt(m);
			}
		}
		return Hashes.asHashList(hashes);
	}

	private SecureRandom getRandom(T value) {
		final SecureRandom random = getRandom();
		final ByteWriteStream out = new ByteWriteStream();
		source.sourceData(value, out);
		random.setSeed(out.getBytes());
		return random;
	}

	private SecureRandom getRandom() {
		try {
			return provider == null ? SecureRandom.getInstance(algorithm) : SecureRandom.getInstance(algorithm, provider);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
	}

}
