package com.tomgibara.crinch.hashing;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import com.tomgibara.crinch.util.DigestWriteStream;

public class DigestHash<T> extends AbstractHash<T> {

	public interface DigestSource {
		
		int getLengthInBytes();
		
		MessageDigest createDigest();
		
	}
	
	public static class StandardDigestSource implements DigestSource {

		private static boolean isCloneable(MessageDigest digest) {
			try {
				digest.clone();
				return true;
			} catch (CloneNotSupportedException e) {
				return false;
			}
		}
		
		private static int lengthInBytes(MessageDigest digest) {
			int length = digest.getDigestLength();
			if (length < 1) throw new IllegalArgumentException("digest length unknown");
			return length;
		}
		
		private final String algorithm;
		private final String provider;
		private final MessageDigest digest;
		final int lengthInBytes;

		public StandardDigestSource(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
			this(algorithm, null, false);
		}

		public StandardDigestSource(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
			this(algorithm, null, true);
		}

		public StandardDigestSource(String algorithm, String provider, boolean providerMandatory) throws NoSuchAlgorithmException, NoSuchProviderException {
			if (algorithm == null) throw new IllegalArgumentException("null algorithm");
			if (providerMandatory && provider == null) throw new IllegalArgumentException("null provider");
			this.algorithm = algorithm;
			this.provider = provider;
			MessageDigest digest = provider == null ? MessageDigest.getInstance(algorithm) : MessageDigest.getInstance(algorithm, provider);
			lengthInBytes = lengthInBytes(digest);
			this.digest = isCloneable(digest) ? digest : null;
		}

		public StandardDigestSource(MessageDigest digest) {
			if (digest == null) throw new IllegalArgumentException("null digest");
			if (!isCloneable(digest)) throw new IllegalArgumentException("digest not cloneable");
			algorithm = digest.getAlgorithm();
			provider = digest.getProvider().getName();
			lengthInBytes = lengthInBytes(digest);
			this.digest = digest;
		}
		
		@Override
		public int getLengthInBytes() {
			return lengthInBytes;
		}
		
		@Override
		public MessageDigest createDigest() {
			if (digest == null) {
				try {
					return provider == null ? MessageDigest.getInstance(algorithm) : MessageDigest.getInstance(algorithm, provider);
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				} catch (NoSuchProviderException e) {
					throw new RuntimeException(e);
				}
			} else {
				try {
					return (MessageDigest) digest.clone();
				} catch (CloneNotSupportedException e) {
					//shouldn't happen
					throw new RuntimeException(e);
				}
			}
		}
	}

	private static DigestSource sha1;
	
	public static DigestSource getSHA1DigestSource() {
		try {
			return sha1 == null ? sha1 = new StandardDigestSource("SHA1") : sha1;
		} catch (NoSuchAlgorithmException e) {
			// unlikely
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// unlikely
			throw new RuntimeException(e);
		}
	}
	
	private final DigestSource digestSource;
	private final HashSource<T> hashSource;
	private final HashRange range;
	
	public DigestHash(DigestSource digestSource, HashSource<T> hashSource) {
		if (digestSource == null) throw new IllegalArgumentException("null digestSource");
		if (hashSource == null) throw new IllegalArgumentException("null source");
		this.digestSource = digestSource;
		this.hashSource = hashSource;
		this.range = new HashRange(BigInteger.ZERO, BigInteger.ONE.shiftLeft(8 * digestSource.getLengthInBytes()).subtract(BigInteger.ONE));
	}
	
	@Override
	public HashRange getRange() {
		return range;
	}
	
	@Override
	public BigInteger hashAsBigInt(T value) {
		MessageDigest digest = digestSource.createDigest();
		DigestWriteStream stream = new DigestWriteStream(digest);
		hashSource.sourceData(value, stream);
		return new BigInteger(1, digest.digest());
	}
	
}
