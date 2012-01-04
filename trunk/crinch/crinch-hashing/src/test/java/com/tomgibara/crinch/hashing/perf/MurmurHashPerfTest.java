package com.tomgibara.crinch.hashing.perf;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.Sink;
import com.tomgibara.crinch.hashing.Hash;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.Murmur3_32Hash;
import com.tomgibara.crinch.util.WriteStream;

import junit.framework.TestCase;

public class MurmurHashPerfTest extends TestCase {

	public static void main(String[] args) throws Exception {
		MurmurHashPerfTest test = new MurmurHashPerfTest();
		test.setUp();
		while(true) {
			test.testAgainstGuava();
		}
	}
	
	private static final Charset ASCII = Charset.forName("ASCII");
	private static final int SIZE = 1000000;
	private static final int REPS = 10;
	private static Pojo[] pojos = null;
	private static int[] hashes = null;
	
	@Override
	protected void setUp() throws Exception {
		if (pojos == null) {
			pojos = new Pojo[SIZE];
			hashes = new int[SIZE];
			Random r = new Random(0L);
			for (int i = 0; i < pojos.length; i++) {
				byte[] bytes = new byte[r.nextInt(30)];
				r.nextBytes(bytes);
				for (int j = 0; j < bytes.length; j++) bytes[j] &= 127;
				Pojo pojo = new Pojo();
				pojo.strVal = new String(bytes, ASCII);
				pojo.intVal = r.nextInt();
				pojo.longVal = r.nextLong();
				pojo.boolVal = r.nextBoolean();
				pojos[i] = pojo;
			}
			System.gc();
		}
	}
	
	public void testAgainstGuava() {
		
		long crinchMedian = medianTime("Crinch", new Runnable() {
			@Override
			public void run() {
				hashAllWithCrinch();
			}
		});
		
		System.gc();
		
		long guavaMedian = medianTime("Guava", new Runnable() {
			@Override
			public void run() {
				hashAllWithGuava();
			}
		});
		
		assertTrue(crinchMedian < guavaMedian);
	}
	
	private long medianTime(String title, Runnable r) {
		long[] times = new long[REPS];
		System.out.println(title);
		for (int i = 0; i < REPS; i++) {
			long startTime = System.currentTimeMillis();
			r.run();
			long finishTime = System.currentTimeMillis();
			long time = finishTime - startTime;
			times[i] = time;
			System.out.println(time + "ms");
		}
		Arrays.sort(times);
		long median = times[REPS / 2];
		System.out.println("MEDIAN: " + median + "ms");
		System.out.println();
		return median;
	}
	
	private void hashAllWithCrinch() {
		Hash<Pojo> hash = new Murmur3_32Hash<Pojo>(new PojoSource());
		for (int i = 0; i < SIZE; i++) {
			hashes[i] = hash.hashAsInt(pojos[i]);
		}
	}
	
	private void hashAllWithGuava() {
		HashFunction hash = Hashing.murmur3_32();
		Funnel<Pojo> funnel = new PojoFunnel();
		for (int i = 0; i < SIZE; i++) {
			hashes[i] = hash.newHasher().putObject(pojos[i], funnel).hash().asInt();
		}
	}
	
	private static class Pojo {
		
		String strVal;
		int intVal;
		long longVal;
		boolean boolVal;
		
	}
	
	private static class PojoSource implements HashSource<Pojo> {
		
		@Override
		public void sourceData(Pojo pojo, WriteStream out) {
			out.writeChars(pojo.strVal);
			out.writeInt(pojo.intVal);
			out.writeLong(pojo.longVal);
			out.writeBoolean(pojo.boolVal);
		}
		
	}
	
	private static class PojoFunnel implements Funnel<Pojo> {
		
		@Override
		public void funnel(Pojo pojo, Sink into) {
			into.putString(pojo.strVal)
				.putInt(pojo.intVal)
				.putLong(pojo.longVal)
				.putBoolean(pojo.boolVal);
		}
		
	}
	
}
