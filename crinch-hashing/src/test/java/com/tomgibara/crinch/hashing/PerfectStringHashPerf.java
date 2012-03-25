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

import java.util.Random;

public class PerfectStringHashPerf {

	public static int sum = 0;
	
	public static void main(String[] args) {

		final int size = 100000;
		final int count = 10000;
		
		timeLongs(size, count);
		timeInts(size, count);
		timeLongs(size, count);
		timeInts(size, count);
		timeLongs(size, count);
		timeInts(size, count);
		timeLongs(size, count);
		timeInts(size, count);
	}
	
	private static void timeInts(final int size, final int count) {
		//prepare
		int sum = 0;
		
		//fill longs with rubbish
		final Random r = new Random();
		final int[] ints = new int[size*2];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = r.nextInt();
		}
		
		long start = System.currentTimeMillis();
		for (int j = 0; j < count; j++) {
			for (int i = 0; i < size; i++) {
				sum += ints[(i<<1)    ];
				sum += ints[(i<<1) + 1];
			}
			PerfectStringHashPerf.sum = sum;
		}
		System.out.println("I: " + (System.currentTimeMillis() - start));
	}
	
	private static void timeLongs(final int size, final int count) {
		//prepare
		int sum = 0;
		
		//fill longs with rubbish
		final Random r = new Random();
		final long[] longs = new long[size];
		for (int i = 0; i < longs.length; i++) {
			longs[i] = r.nextLong();
		}
		
		long start = System.currentTimeMillis();
		for (int j = 0; j < count; j++) {
			for (int i = 0; i < size; i++) {
				final long l = longs[i];
				sum += (int) (l >> 32);
				sum += (int) l;
			}
			PerfectStringHashPerf.sum = sum;
		}
		System.out.println("L: " + (System.currentTimeMillis() - start));
	}
	
}
