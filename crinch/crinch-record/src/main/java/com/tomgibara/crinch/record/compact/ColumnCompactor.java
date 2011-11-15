package com.tomgibara.crinch.record.compact;

import java.math.BigDecimal;
import java.util.Arrays;

import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.ColumnStats.Classification;

class ColumnCompactor {

	private final ColumnStats stats;
	// derived from stats
	private final boolean nullable;
	private final long offset;
	private final boolean enumerated;
	private final String[] enumeration;
	
	private final HuffmanCoding huffman;
	
	ColumnCompactor(ColumnStats stats) {
		this.stats = stats;
		nullable = stats.isNullable();
		enumerated = stats.getClassification() == Classification.ENUMERATED;
		enumeration = enumerated ? stats.getEnumeration() : null;
		
		switch (stats.getClassification()) {
		case INTEGRAL:
		case TEXTUAL:
			this.offset = stats.getSum().divideToIntegralValue(BigDecimal.valueOf(stats.getCount())).longValue();
			break;
			default:
				this.offset = 0L;
		}
		long[] freqs = stats.getFrequencies();
		if (freqs == null) {
			huffman = null;
		} else {
			huffman = new HuffmanCoding(new HuffmanCoding.UnorderedFrequencyValues(freqs));
		}
	}

	ColumnStats getStats() {
		return stats;
	}
	
	int encodeNull(CodedWriter writer, boolean isNull) {
		if (!nullable) return 0;
		return writer.getWriter().writeBoolean(isNull);
	}
	
	boolean decodeNull(CodedReader reader) {
		if (!nullable) return false;
		return reader.getReader().readBoolean();
	}
	
	int encodeString(CodedWriter writer, String value) {
		BitWriter w = writer.getWriter();
		if (enumerated) {
			int i;
			if (enumeration == null) {
				i = value.charAt(0);
			} else {
				i = Arrays.binarySearch(enumeration, value);
			}
			if (i < 0) throw new IllegalArgumentException("Not enumerated: " + value);
			return huffman.encodePositiveInt(w, i + 1);
		} else {
			int length = value.length();
			int n = writer.writeSignedInt(length - (int) offset);
			for (int i = 0; i < length; i++) {
				char c = value.charAt(i);
				n += huffman.encodePositiveInt(w, c + 1);
			}
			return n;
		}
	}
	
	String decodeString(CodedReader reader) {
		if (enumerated) {
			int value = huffman.decodePositiveInt(reader.getReader()) - 1;
			if (enumeration == null) {
				return Character.toString((char) value);
			} else {
				return enumeration[value];
			}
		} else {
			int length = ((int) offset) + reader.readSignedInt();
			StringBuilder sb = new StringBuilder(length);
			for (; length > 0; length--) {
				sb.append((char) (huffman.decodePositiveInt(reader.getReader()) - 1));
			}
			return sb.toString();
		}
	}
	
	int encodeChar(CodedWriter writer, char value) {
		return huffman.encodePositiveInt(writer.getWriter(), value + 1);
	}
	
	char decodeChar(CodedReader reader) {
		return (char) (huffman.decodePositiveInt(reader.getReader()) - 1);
	}
	
	int encodeInt(CodedWriter writer, int value) {
		return writer.writeSignedInt(value - (int) offset);
	}
	
	int decodeInt(CodedReader reader) {
		return ((int) offset) + reader.readSignedInt();
	}
	
	int encodeLong(CodedWriter writer, long value) {
		return writer.writeSignedLong(value - offset);
	}
	
	long decodeLong(CodedReader reader) {
		return offset + reader.readSignedLong();
	}
	
	int encodeBoolean(CodedWriter writer, boolean value) {
		return huffman.encodePositiveInt(writer.getWriter(), value ? 1 : 2);
	}
	
	boolean decodeBoolean(CodedReader reader) {
		return huffman.decodePositiveInt(reader.getReader()) == 1;
	}
	
	int encodeFloat(CodedWriter writer, float value) {
		//TODO support float methods directly when made available
		return writer.writeDouble(value);
	}
	
	float decodeFloat(CodedReader reader) {
		//TODO support float methods directly when made available
		return (float) reader.readDouble();
	}
	
	int encodeDouble(CodedWriter writer, double value) {
		return writer.writeDouble(value);
	}
	
	double decodeDouble(CodedReader reader) {
		return reader.readDouble();
	}

	@Override
	public String toString() {
		return stats.toString();
	}
}
