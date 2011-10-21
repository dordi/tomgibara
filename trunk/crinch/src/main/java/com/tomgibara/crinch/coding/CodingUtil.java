package com.tomgibara.crinch.coding;

import java.util.Arrays;
import java.util.HashMap;

public class CodingUtil {
/*
	private static final double LOG_2_RECIP = 1.0/Math.log(2);
	private static final int MAX_FREQ_SIZE = 16384;
	
	private static double calculateEntropyFromFrequencies(int length, int[] frequencies, HashMap<Integer, Integer> overflow) {
		if (length < 0) {
			length = 0;
			for (int i = 0; i < frequencies.length; i++) {
				length += frequencies[i];
			}
			if (overflow != null) length += overflow.size();
		}
		if (length == 0) return 0.0;
		
		double c = length;
		double sum = 0.0;
		for (int f : frequencies) {
			if (f == 0) continue;
			double p = f/c;
			sum -= p * Math.log(p)*LOG_2_RECIP;
		}
		if (overflow != null) {
			for (int f : overflow.values()) {
				double p = f/c;
				sum -= p * Math.log(p)*LOG_2_RECIP;
			}
		}
		return sum;
	}

	public static double calculateEntropyFromFrequencies(int[] frequencies) {
		return calculateEntropyFromFrequencies(-1, frequencies, null);
	}
	
	public static double calculateEntropyFromValues(byte[] values, int offset, int length) {
		int[] freqs = new int[256];
		for (int i = 0; i < values.length; i++) {
			freqs[values[i] & 0xff]++;
		}
		return calculateEntropyFromFrequencies(freqs);
	}

	public static double calculateEntropyFromValues(byte[] values) {
		return calculateEntropyFromValues(values, 0, values.length);
	}
	

	public static double calculateEntropyFromValues(int[] values, int offset, int length) {
		int[] freq = new int[256];
		HashMap<Integer, Integer> overflow = null;
		for (int i = 0; i < values.length; i++) {
			int value = values[i];
			value = value < 0 ? -(value<<1) - 1 : value<<1;
			if (value >= freq.length) {
				if (overflow == null) {
					int newlength = Integer.highestOneBit(value-1) << 1; 
					if (newlength > MAX_FREQ_SIZE) {
						overflow = new HashMap<Integer, Integer>();
						Integer o = overflow.get(value);
						Integer n = o == null ? 1 : o+1;
						overflow.put(value, n);
					} else {
						freq = Arrays.copyOf(freq, newlength);
						freq[value]++;
					}
				} else {
					Integer o = overflow.get(value);
					Integer n = o == null ? 1 : o+1;
					overflow.put(value, n);
				}
			} else {
				freq[value]++;
			} 
		}
		return calculateEntropyFromFrequencies(length, freq, overflow);
	}

	public static double calculateEntropyFromValues(int[] values) {
		return calculateEntropyFromValues(values, 0, values.length);
	}
	*/
}
