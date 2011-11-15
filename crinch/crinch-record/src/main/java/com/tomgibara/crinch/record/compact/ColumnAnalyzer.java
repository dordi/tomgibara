package com.tomgibara.crinch.record.compact;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tomgibara.crinch.coding.CharFrequencyRecorder;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.ColumnStats.Classification;

//TODO must switch to using parser when safe parsing methods have been separated out
//TODO many opportunities for overflow in this class
abstract class ColumnAnalyzer {

	public static ColumnAnalyzer newInstance(ColumnType type) {
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
	
	final ColumnType type;
	boolean nullable;
	
	abstract void analyze(String str);
	
	abstract ColumnStats stats();
	
	ColumnAnalyzer(ColumnType type) {
		this.type = type;
	}
	
	// primitive implementations
	
	private static class DoubleAnalyzer extends ColumnAnalyzer {
		
		private double sum = 0.0;
		private long count = 0L;
		private double minValue = Double.MAX_VALUE;
		private double maxValue = Double.MIN_VALUE;
		
		DoubleAnalyzer(ColumnType type) {
			super(type);
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
			}
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
			return stats;
		}
		
	}
	
	private static class BooleanAnalyzer extends ColumnAnalyzer {
		
		private final Set<String> flags = new HashSet<String>(Arrays.asList("t", "T", "true", "TRUE", "True", "1", "Y", "y", "Yes"));
		
		long trues = 0L;
		long falses = 0L;
		
		BooleanAnalyzer(ColumnType type) {
			super(type);
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
			stats.setFrequencies(new long[] {falses, trues});
			stats.setEnumeration(new String[] {"false", "true"});
			return stats;
		}
		
	}

	private static class SmallIntAnalyzer extends ColumnAnalyzer {

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
			case CHAR_PRIMITIVE:
			case CHAR_WRAPPER:
				minValue = Character.MIN_VALUE;
				maxValue = Character.MAX_VALUE;
				break;
			default:
				throw new IllegalArgumentException("invalid type:" + type);
			}
			offset = minValue;
			freqs = new long[maxValue - minValue + 1];
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
			return stats;
		}
	}

	private static class LargeIntAnalyzer extends ColumnAnalyzer {

		private long sum = 0L;
		private long count = 0L;
		private long minValue = Long.MAX_VALUE;
		private long maxValue = Long.MIN_VALUE;

		LargeIntAnalyzer(ColumnType type) {
			super(type);
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
			}
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
			return stats;
		}

	}
	
	private static class CharAnalyzer extends ColumnAnalyzer {

		private final CharFrequencyRecorder cfr = new CharFrequencyRecorder();
		
		CharAnalyzer(ColumnType type) {
			super(type);
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
			stats.setFrequencies(cfr.getFrequencies());
			return stats;
		}
		
	}
	
	private static class StringAnalyzer extends ColumnAnalyzer {
		
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
			super(type);
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				cfr.record(str);
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
			return stats;
		}

	}
	
}
