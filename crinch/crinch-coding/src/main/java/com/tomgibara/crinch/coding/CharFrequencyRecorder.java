/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.coding;

import java.util.Arrays;

/**
 * Records the frequencies of characters occurring in Strings, arrays and possibly other sources.
 * 
 * @author Tom Gibara
 *
 */

//TODO add a method that records from CharSequence
//TODO add a method records characters from an array range
public class CharFrequencyRecorder {

	private long[] frequencies = new long[128]; // assume ASCII to start
	
	/**
	 * Observes the occurrence of single character.
	 * 
	 * @param c
	 *            a character for which the frequency should be incremented
	 */
	
	public void record(char c) {
		ensureLength(c);
		frequencies[c] ++;
	}

	/**
	 * Observes the occurrence of characters in an array.
	 * 
	 * @param cs
	 *            an array of characters
	 * @throws IllegalArgumentException
	 *             if the array of characters is null
	 */
	
	public void record(char[] cs) {
		if (cs == null) throw new IllegalArgumentException("null cs");
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

	/**
	 * Observes the occurrence of characters in a String.
	 * 
	 * @param str
	 *            a string
	 * @throws IllegalArgumentException
	 *             if the String is null
	 */
	
	public void record(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
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
	
	/**
	 * The frequencies of the observed characters. The frequency of a character
	 * c is stored in the cth index. The length of the array may be less than
	 * the number of possible characters. If the value of a character is greater
	 * than or equal to the length of the frequency array, it implies that the
	 * frequency of the character is zero.
	 * 
	 * The returned array is owned by the caller.
	 * 
	 * @return an array of the observed character frequencies.
	 */
	
	public long[] getFrequencies() {
		return frequencies.clone();
	}

	/**
	 * The total number of characters that have been observed.
	 * 
	 * @return the total of all frequencies
	 */
	
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
