package com.tomgibara.crinch.coding;

import java.util.Arrays;

public class CharFrequencyRecorder {

	private long[] frequencies = new long[128]; // assume ASCII to start
	
	public void record(char c) {
		ensureLength(c);
		frequencies[c] ++;
	}

	public void record(char[] cs) {
		switch (cs.length) {
		case 0 : return;
		case 1 : record(cs[0]); return;
		default :
			int m = cs[0];
			for (int i = 0; i < cs.length; i++) {
				int c = cs[i];
				if (m < c) m = c;
			}
			ensureLength(m);
			for (int i = 0; i < cs.length; i++) {
				frequencies[cs[i]]++;
			}
		}
	}
	
	public void record(String str) {
		int length = str.length();
		switch (length) {
		case 0 : return;
		case 1 : record(str.charAt(0)); return;
		default :
			int m = str.charAt(0);
			for (int i = 0; i < length; i++) {
				int c = str.charAt(i);
				if (m < c) m = c;
			}
			ensureLength(m);
			for (int i = 0; i < length; i++) {
				frequencies[str.charAt(i)]++;
			}
		}
	}
	
	public long[] getFrequencies() {
		return frequencies;
	}
	
	public long getFrequencyTotal() {
		long total = 0L;
		for (int i = 0; i < frequencies.length; i++) {
			total += frequencies[i];
		}
		return total;
	}
	
	private void ensureLength(int c) {
		if (c >= frequencies.length) {
			frequencies = Arrays.copyOfRange(frequencies, 0, c + 1);
		}
	}
	
}
