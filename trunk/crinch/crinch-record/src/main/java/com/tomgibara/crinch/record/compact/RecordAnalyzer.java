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
package com.tomgibara.crinch.record.compact;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tomgibara.crinch.coding.CharFrequencyRecorder;
import com.tomgibara.crinch.collections.BasicBloomFilter;
import com.tomgibara.crinch.collections.BloomFilter;
import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.hashing.ObjectMultiHash;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.ColumnStats.Classification;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.process.ProcessContext;

//TODO bloom filter parameters should be determined on a column-by-column basis
//TODO avg length of string columns should inform bloom filter params
class RecordAnalyzer {

	private static final double LOG_2 = Math.log(2);
	private static final int BLOOM_MIN_SIZE = 256;
	private static final double BLOOM_PROB = 0.001;

	private static boolean isFreqsUnique(long[] freqs) {
		for (int i = 0; i < freqs.length; i++) {
			if (freqs[i] > 1L) return false;
		}
		return true;
	}
	
	private final long recordCount;
	private final ColumnAnalyzer[] analyzers;
	private final int hashCount;
	private final MultiHash<Long> longMultiHash; 
	private final MultiHash<Double> dblMultiHash; 
	private final MultiHash<String> strMultiHash;
	
	private int passCount = 0;
	

	RecordAnalyzer(ProcessContext context) {
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalArgumentException("context has no column types");
		recordCount = context.getRecordCount();
		if (recordCount == -1L) throw new IllegalArgumentException("context has no record count");
		analyzers = new ColumnAnalyzer[types.size()];
		int bloomSize = Math.max(BLOOM_MIN_SIZE, (int) Math.min(Integer.MAX_VALUE, Math.round(recordCount * - Math.log(BLOOM_PROB) / (LOG_2 * LOG_2))));
		hashCount = Math.max(1, Math.round( (float) LOG_2 * bloomSize / recordCount) );
		longMultiHash = new ObjectMultiHash<Long>(bloomSize - 1);
		dblMultiHash = new ObjectMultiHash<Double>(bloomSize - 1);
		strMultiHash = new ObjectMultiHash<String>(bloomSize - 1);
		newAnalyzers(types);
	}

	boolean needsReanalysis() {
		if (passCount == 1) return false;
		passCount ++;
		for (int i = 0; i < analyzers.length; i++) {
			if (analyzers[i].needsReanalysis()) return true;
		}
		return false;
	}
	
	void analyze(LinearRecord record) {
		for (int i = 0; i < analyzers.length; i++) {
			CharSequence chars = record.nextString();
			ColumnAnalyzer analyzer = analyzers[i];
			String str = chars == null ? null : chars.toString();
			switch (passCount) {
			case 0 : analyzer.analyze(str); break;
			case 1 : analyzer.reanalyze(str); break;
			default : throw new IllegalStateException("too many passes");
			}
		}
	}
	
	RecordStats getStats() {
		RecordStats stats = new RecordStats();
		stats.setRecordCount(recordCount);
		final int length = analyzers.length;
		List<ColumnStats> list = stats.getColumnStats();
		for (int i = 0; i < length; i++) {
			list.add( analyzers[i].stats() );
		}
		return stats;
	}
	
	@Override
	public String toString() {
		String nl = String.format("%n");
		StringBuilder sb = new StringBuilder();
		for (ColumnAnalyzer analyzer : analyzers) {
			if (sb.length() > 0) sb.append(nl);
			sb.append(analyzer.stats());
		}
		return sb.toString();
	}

	// column analyzers

	private void newAnalyzers(List<ColumnType> types) {
		for (int i = 0; i < analyzers.length; i++) {
			analyzers[i] = newAnalyzer(types.get(i));
		}
	}
	
	private ColumnAnalyzer newAnalyzer(ColumnType type) {
		switch (type) {
		case BOOLEAN_PRIMITIVE:
		case BOOLEAN_WRAPPER:
			return new BooleanAnalyzer(type);
		case BYTE_PRIMITIVE:
		case BYTE_WRAPPER:
		case SHORT_PRIMITIVE:
		case SHORT_WRAPPER:
			return new SmallIntAnalyzer(type);
		case INT_PRIMITIVE:
		case INT_WRAPPER:
		case LONG_PRIMITIVE:
		case LONG_WRAPPER:
			return new LargeIntAnalyzer(type);
		case FLOAT_PRIMITIVE:
		case FLOAT_WRAPPER:
		case DOUBLE_PRIMITIVE:
		case DOUBLE_WRAPPER:
			return new DoubleAnalyzer(type);
		case CHAR_PRIMITIVE:
		case CHAR_WRAPPER:
			return new CharAnalyzer(type);
		case STRING_OBJECT:
			return new StringAnalyzer(type);
			default: throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}

	// inner classes
	
	private abstract class ColumnAnalyzer {

		final ColumnType type;
		boolean nullable;
		
		abstract boolean needsReanalysis();
		
		abstract void analyze(String str);

		void reanalyze(String str) { }
		
		abstract ColumnStats stats();
		
		ColumnAnalyzer(ColumnType type) {
			this.type = type;
		}
		
	}
	
	private abstract class FilteredAnalyzer<T> extends ColumnAnalyzer {

		BloomFilter<T> filter;
		//TODO need a primitive collection for these
		Set<T> candidates;
		Set<T> verification = null;
		boolean unique = true;


		FilteredAnalyzer(ColumnType type, MultiHash<T> multiHash) {
			super(type);
			filter = new BasicBloomFilter<T>(multiHash, hashCount);
			candidates = new HashSet<T>();
		}

		//TODO very ugly mutating on getter
		@Override
		boolean needsReanalysis() {
			filter = null;
			verification = new HashSet<T>();
			return candidates != null && !candidates.isEmpty();
		}

		@Override
		void reanalyze(String str) {
			if (str == null || candidates == null) return;
			T value = parse(str);
			if (!candidates.contains(value)) return;
			if (!verification.add(value)) {
				candidates = null;
				verification = null;
				unique = false;
			}
		}

		abstract T parse(String str);
		
		void filter(T value) {
			if (!filter.add(value) && !candidates.add(value)) {
				// we've found a definite dupe
				// release memory (also prevents further analysis)
				candidates = null;
				filter = null;
				unique = false;
			}
		}
		
	}

	private class DoubleAnalyzer extends FilteredAnalyzer<Double> {
		
		private double sum = 0.0;
		private long count = 0L;
		private double minValue = Double.MAX_VALUE;
		private double maxValue = Double.MIN_VALUE;
		
		DoubleAnalyzer(ColumnType type) {
			super(type, dblMultiHash);
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				double value = Double.parseDouble(str);
				sum += value;
				count++;
				minValue = Math.min(value, minValue);
				maxValue = Math.max(value, maxValue);
				if (filter != null) filter(value);
			}
		}
		
		@Override
		Double parse(String str) {
			return Double.parseDouble(str);
		}
		
		@Override
		ColumnStats stats() {
			ColumnStats stats = new ColumnStats();
			stats.setNullable(nullable);
			stats.setClassification(Classification.FLOATING);
			stats.setMinimum(BigDecimal.valueOf(minValue));
			stats.setMaximum(BigDecimal.valueOf(maxValue));
			stats.setSum(BigDecimal.valueOf(sum));
			stats.setCount(count);
			stats.setFrequencies(null);
			stats.setUnique(unique);
			return stats;
		}
		
	}
	
	private class BooleanAnalyzer extends ColumnAnalyzer {
		
		private final Set<String> flags = new HashSet<String>(Arrays.asList("t", "T", "true", "TRUE", "True", "1", "Y", "y", "Yes"));
		
		long trues = 0L;
		long falses = 0L;
		
		BooleanAnalyzer(ColumnType type) {
			super(type);
		}

		@Override
		boolean needsReanalysis() {
			return false;
		}
		
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				boolean value = flags.contains(str);
				if (value) {
					trues++;
				} else {
					falses++;
				}
			}
		}
		
		@Override
		ColumnStats stats() {
			ColumnStats stats = new ColumnStats();
			stats.setNullable(nullable);
			stats.setClassification(Classification.ENUMERATED);
			stats.setMinimum(falses == 0L ? BigDecimal.ONE : BigDecimal.ZERO );
			stats.setMaximum(trues == 0L ? BigDecimal.ZERO : BigDecimal.ONE );
			stats.setSum(BigDecimal.valueOf(trues));
			stats.setCount(trues + falses);
			long[] freqs = new long[] {falses, trues};
			stats.setFrequencies(freqs);
			stats.setEnumeration(new String[] {"false", "true"});
			stats.setUnique(isFreqsUnique(freqs));
			return stats;
		}
		
	}

	private class SmallIntAnalyzer extends ColumnAnalyzer {

		private final int offset;
		private final long[] freqs;

		private int minValue = Integer.MAX_VALUE;
		private int maxValue = Integer.MIN_VALUE;

		SmallIntAnalyzer(ColumnType type) {
			super(type);
			final int minValue;
			final int maxValue;
			switch (type) {
			case BYTE_PRIMITIVE:
			case BYTE_WRAPPER:
				minValue = Byte.MIN_VALUE;
				maxValue = Byte.MAX_VALUE;
				break;
			case SHORT_PRIMITIVE:
			case SHORT_WRAPPER:
				minValue = Short.MIN_VALUE;
				maxValue = Short.MAX_VALUE;
				break;
			default:
				throw new IllegalArgumentException("invalid type:" + type);
			}
			offset = minValue;
			freqs = new long[maxValue - minValue + 1];
		}

		@Override
		boolean needsReanalysis() {
			return false;
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				int value = Integer.parseInt(str);
				freqs[value - offset]++;
				minValue = Math.min(value, minValue);
				maxValue = Math.max(value, maxValue);
			}
		}

		@Override
		ColumnStats stats() {
			long sum = 0L;
			long count = 0L;
			for (int i = 0; i < freqs.length; i++) {
				long f = freqs[i];
				count += f;
				sum += i * f;
			}
			ColumnStats stats = new ColumnStats();
			stats.setNullable(nullable);
			stats.setClassification(Classification.INTEGRAL);
			stats.setMinimum(BigDecimal.valueOf(minValue));
			stats.setMaximum(BigDecimal.valueOf(maxValue));
			stats.setSum(BigDecimal.valueOf(sum));
			stats.setCount(count);
			stats.setFrequencies(freqs);
			stats.setUnique(isFreqsUnique(freqs));
			return stats;
		}
	}

	private class LargeIntAnalyzer extends FilteredAnalyzer<Long> {

		private long sum = 0L;
		private long count = 0L;
		private long minValue = Long.MAX_VALUE;
		private long maxValue = Long.MIN_VALUE;
		
		LargeIntAnalyzer(ColumnType type) {
			super(type, longMultiHash);
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				long value = Long.parseLong(str);
				sum += value;
				count++;
				minValue = Math.min(value, minValue);
				maxValue = Math.max(value, maxValue);
				if (filter != null) filter(value);
			}
		}
		
		@Override
		Long parse(String str) {
			return Long.parseLong(str);
		}
		
		@Override
		ColumnStats stats() {
			ColumnStats stats = new ColumnStats();
			stats.setNullable(nullable);
			stats.setClassification(Classification.INTEGRAL);
			stats.setMinimum(BigDecimal.valueOf(minValue));
			stats.setMaximum(BigDecimal.valueOf(maxValue));
			stats.setSum(BigDecimal.valueOf(sum));
			stats.setCount(count);
			stats.setFrequencies(null);
			stats.setUnique(unique);
			return stats;
		}

	}
	
	private class CharAnalyzer extends ColumnAnalyzer {

		private final CharFrequencyRecorder cfr = new CharFrequencyRecorder();
		
		CharAnalyzer(ColumnType type) {
			super(type);
		}

		@Override
		boolean needsReanalysis() {
			return false;
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				char c = str.charAt(0);
				cfr.record(c);
			}
		}

		@Override
		ColumnStats stats() {
			ColumnStats stats = new ColumnStats();
			stats.setNullable(nullable);
			stats.setMinimum(BigDecimal.ONE);
			stats.setMaximum(BigDecimal.ONE);
			long count = cfr.getFrequencyTotal();
			stats.setCount(count);
			stats.setSum(BigDecimal.valueOf(count));
			stats.setClassification(Classification.ENUMERATED);
			long[] freqs = cfr.getFrequencies();
			stats.setFrequencies(freqs);
			stats.setUnique(isFreqsUnique(freqs));
			return stats;
		}
		
	}
	
	private class StringAnalyzer extends FilteredAnalyzer<String> {
		
		//TODO make configurable
		private final static int MAX_ENUM = 1024;
		
		private final CharFrequencyRecorder cfr = new CharFrequencyRecorder();
		
		private int enumCount = 0;
		private String[] enumValues = new String[MAX_ENUM];
		private long[] enumFreqs = new long[MAX_ENUM];
		
		private long lengthSum = 0L;
		private long count = 0L;
		private int minValue = Integer.MAX_VALUE;
		private int maxValue = Integer.MIN_VALUE;

		StringAnalyzer(ColumnType type) {
			super(type, strMultiHash);
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				cfr.record(str);
				if (filter != null) filter(str);
				int value = str.length();
				lengthSum += value;
				count++;
				minValue = Math.min(value, minValue);
				maxValue = Math.max(value, maxValue);
				if (enumValues != null) {
					//TODO make something more efficient
					int i = Arrays.binarySearch(enumValues, 0, enumCount, str);
					if (i < 0) {
						if (enumCount == MAX_ENUM) {
							enumValues = null;
							enumFreqs = null;
							enumCount = -1;
						} else {
							i =  -i - 1;
							System.arraycopy(enumValues, i, enumValues, i+1, enumCount - i);
							System.arraycopy(enumFreqs, i, enumFreqs, i+1, enumCount - i);
							enumValues[i] = str;
							enumFreqs[i] = 1L;
							enumCount++;
						}
					} else {
						enumFreqs[i]++;
					}
				}
			}
		}
		
		@Override
		String parse(String str) {
			return str;
		}
		
		@Override
		ColumnStats stats() {
			ColumnStats stats = new ColumnStats();
			stats.setNullable(nullable);
			stats.setMinimum(BigDecimal.valueOf(minValue));
			stats.setMaximum(BigDecimal.valueOf(maxValue));
			stats.setSum(BigDecimal.valueOf(lengthSum));
			stats.setCount(count);
			if (enumFreqs == null) {
				stats.setClassification(Classification.TEXTUAL);
				stats.setFrequencies(cfr.getFrequencies());
			} else {
				stats.setClassification(Classification.ENUMERATED);
				stats.setEnumeration(Arrays.copyOfRange(enumValues, 0, enumCount));
				stats.setFrequencies(Arrays.copyOfRange(enumFreqs, 0, enumCount));
			}
			stats.setUnique(unique);
			return stats;
		}

	}

	
}
