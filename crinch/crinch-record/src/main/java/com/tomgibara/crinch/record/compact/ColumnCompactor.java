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

import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.HuffmanCoding;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.ColumnStats.Classification;

class ColumnCompactor {

	private final ColumnStats stats;
	private final CompactCharStore store;
	private final int columnIndex;
	// derived from stats
	private final boolean nullable;
	private final long offset;
	private final boolean enumerated;
	private final String[] enumeration;
	
	private final HuffmanCoding huffman;
	
	//TODO nasty constructor
	ColumnCompactor(ColumnStats stats, CompactCharStore store, int columnIndex) {
		this.stats = stats;
		this.store = store;
		this.columnIndex = columnIndex;
		
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
			huffman = new HuffmanCoding(new HuffmanCoding.UnorderedFrequencies(freqs));
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
	
	int encodeString(CodedWriter writer, CharSequence value) {
		BitWriter w = writer.getWriter();
		if (enumerated) {
			int i;
			if (enumeration == null) {
				i = value.charAt(0);
			} else {
				i = Arrays.binarySearch(enumeration, value);
			}
			if (i < 0) throw new IllegalArgumentException("Not enumerated: " + value);
			return huffman.encodePositiveInt(w, i);
		} else {
			int length = value.length();
			int n = writer.writeInt(length - (int) offset);
			for (int i = 0; i < length; i++) {
				char c = value.charAt(i);
				n += huffman.encodePositiveInt(w, c);
			}
			return n;
		}
	}
	
	CharSequence decodeString(CodedReader reader) {
		if (enumerated) {
			int value = huffman.decodePositiveInt(reader.getReader());
			if (enumeration == null) {
				return Character.toString((char) value);
			} else {
				return enumeration[value];
			}
		} else {
			int length = ((int) offset) + reader.readInt();
			CompactCharSequence chars = store.getChars(columnIndex, length);
			for (; length > 0; length--) {
				char c = (char) (huffman.decodePositiveInt(reader.getReader()));
				chars.append(c);
			}
			return chars;
		}
	}
	
	int encodeChar(CodedWriter writer, char value) {
		return huffman.encodePositiveInt(writer.getWriter(), value);
	}
	
	char decodeChar(CodedReader reader) {
		return (char) huffman.decodePositiveInt(reader.getReader());
	}
	
	int encodeInt(CodedWriter writer, int value) {
		return writer.writeInt(value - (int) offset);
	}
	
	int decodeInt(CodedReader reader) {
		return ((int) offset) + reader.readInt();
	}
	
	int encodeLong(CodedWriter writer, long value) {
		return writer.writeLong(value - offset);
	}
	
	long decodeLong(CodedReader reader) {
		return offset + reader.readLong();
	}
	
	int encodeBoolean(CodedWriter writer, boolean value) {
		return huffman.encodePositiveInt(writer.getWriter(), value ? 0 : 1);
	}
	
	boolean decodeBoolean(CodedReader reader) {
		return huffman.decodePositiveInt(reader.getReader()) == 0;
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
