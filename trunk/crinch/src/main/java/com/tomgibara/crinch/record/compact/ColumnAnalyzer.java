package com.tomgibara.crinch.record.compact;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tomgibara.crinch.coding.CharFrequencyRecorder;

//TODO must switch to using parser when safe parsing methods have been separated out
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
		case CHAR_PRIMITIVE:
		case CHAR_WRAPPER:
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
			return new NullableAnalyzer(type);
		case STRING_OBJECT:
			return new StringAnalyzer(type);
			default: throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}
	
	final ColumnType type;
	boolean nullable;
	
	abstract void analyze(String str);
	
	abstract ColumnCompactor compactor();
	
	ColumnAnalyzer(ColumnType type) {
		this.type = type;
	}
	
	// primitive implementations
	
	private static class NullableAnalyzer extends ColumnAnalyzer {
		
		NullableAnalyzer(ColumnType type) {
			super(type);
		}
		
		@Override
		void analyze(String str) {
			if (str == null) nullable = true;
		}
		
		@Override
		ColumnCompactor compactor() {
			return new ColumnCompactor(type, nullable, 0L, null);
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
		ColumnCompactor compactor() {
			return new ColumnCompactor(type, nullable, 0, null);
		}
		
	}

	private static class SmallIntAnalyzer extends ColumnAnalyzer {

		private final int offset;
		private final long[] freqs;
		
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
			}
		}

		@Override
		ColumnCompactor compactor() {
			return new ColumnCompactor(type, nullable, 0, null);
		}
		
	}

	private static class LargeIntAnalyzer extends ColumnAnalyzer {

		//TODO may overflow
		private long sum = 0L;
		private long count = 0L;

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
			}
		}

		@Override
		ColumnCompactor compactor() {
			return new ColumnCompactor(type, nullable, sum / count, null);
		}

	}
	
	private static class StringAnalyzer extends ColumnAnalyzer {
		
		private final CharFrequencyRecorder cfr = new CharFrequencyRecorder();

		private long lengthSum = 0L;
		private long count = 0L;

		public StringAnalyzer(ColumnType type) {
			super(type);
		}
		
		@Override
		void analyze(String str) {
			if (str == null) {
				nullable = true;
			} else {
				cfr.record(str);
				lengthSum += str.length();
				count++;
			}
		}
		
		@Override
		ColumnCompactor compactor() {
			return new ColumnCompactor(type, nullable, lengthSum / count, cfr.getFrequencies());
		}
		
	}
	
}
