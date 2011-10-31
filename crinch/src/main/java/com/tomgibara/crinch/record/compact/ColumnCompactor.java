package com.tomgibara.crinch.record.compact;

import java.math.BigDecimal;
import java.util.Arrays;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.EliasOmegaEncoding;
import com.tomgibara.crinch.coding.Huffman;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.ColumnStats.Classification;

class ColumnCompactor {

	private static int[][] rank(long[] fs) {
		int len = fs.length;
		El[] els = new El[len];
		for (int i = 0; i < len; i++) {
			els[i] = new El(i, fs[i]);
		}
		Arrays.sort(els);
		int lim;
		for (lim = 0; lim < len; lim++) {
			//if (els[lim].freq == 0) break;
		}
		
		int[][] rs = new int[2][lim];
		for (int i = 0; i < lim; i++) {
			El el = els[i];
			fs[i] = el.freq;
			int j = el.index;
			rs[0][i] = j;
			rs[1][j] = i;
		}
		return rs;
	}
	
	private static class El implements Comparable<El> {
		
		int index;
		long freq;
		
		El(int index, long freq) {
			this.index = index;
			this.freq = freq;
		}
		
		@Override
		public int compareTo(El that) {
			if (this.freq == that.freq) return 0;
			return this.freq < that.freq ? 1 : -1;
		}
		
	}
	
	private final ColumnStats stats;
	// derived from stats
	private final boolean nullable;
	private final long offset;
	private final String[] enumeration;
	
	private final int[][] lookup;
	private final Huffman huffman;
	
	ColumnCompactor(ColumnStats stats) {
		this.stats = stats;
		this.nullable = stats.isNullable();
		this.enumeration = stats.getClassification() == Classification.ENUMERATED ? stats.getEnumeration() : null;
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
			lookup = null;
			huffman = null;
		} else {
			long[] fs = freqs.clone();
			lookup = rank(fs);
			for (int i = 0; i < fs.length; i++) {
				if (fs[i] == 0L) {
					fs = Arrays.copyOfRange(fs, 0, i);
					break;
				}
			}
			//System.out.println("*** " + Arrays.toString(fs));
			//fs = Arrays.copyOfRange(fs, 0, lookup[0].length);
			huffman = new Huffman(fs);
		}
	}

	ColumnStats getStats() {
		return stats;
	}
	
	int encodeNull(BitWriter writer, boolean isNull) {
		if (!nullable) return 0;
		return writer.writeBoolean(isNull);
	}
	
	boolean decodeNull(BitReader reader) {
		if (!nullable) return false;
		return reader.readBoolean();
	}
	
	int encodeString(BitWriter writer, String value) {
		if (enumeration != null) {
			int i = Arrays.binarySearch(enumeration, value);
			if (i < 0) throw new IllegalArgumentException("Not enumerated: " + value);
			return huffman.encode(lookup[1][i]+1, writer);
		} else {
			int length = value.length();
			int n = EliasOmegaEncoding.encodeSignedInt(length - (int) offset, writer);
			for (int i = 0; i < length; i++) {
				char c = value.charAt(i);
				n += huffman.encode(lookup[1][c]+1, writer);
			}
			return n;
		}
	}
	
	String decodeString(BitReader reader) {
		if (enumeration != null) {
			return enumeration[ lookup[0][huffman.decode(reader)-1] ];
		} else {
			int length = ((int) offset) + EliasOmegaEncoding.decodeSignedInt(reader);
			StringBuilder sb = new StringBuilder(length);
			for (; length > 0; length--) {
				sb.append( (char) lookup[0][huffman.decode(reader)-1] );
			}
			return sb.toString();
		}
	}
	
	int encodeInt(BitWriter writer, int value) {
		return EliasOmegaEncoding.encodeSignedInt(value - (int) offset, writer);
	}
	
	int decodeInt(BitReader reader) {
		return ((int) offset) + EliasOmegaEncoding.decodeSignedInt(reader);
	}
	
	int encodeLong(BitWriter writer, long value) {
		return EliasOmegaEncoding.encodeSignedLong(value - offset, writer);
	}
	
	long decodeLong(BitReader reader) {
		return offset + EliasOmegaEncoding.decodeSignedLong(reader);
	}
	
	int encodeBoolean(BitWriter writer, boolean value) {
		return writer.writeBoolean(value);
	}
	
	boolean decodeBoolean(BitReader reader) {
		return reader.readBoolean();
	}
	
	int encodeFloat(BitWriter writer, float value) {
		//TODO support float methods directly when made available
		return EliasOmegaEncoding.encodeDouble(value, writer);
	}
	
	float decodeFloat(BitReader reader) {
		//TODO support float methods directly when made available
		return (float) EliasOmegaEncoding.decodeDouble(reader);
	}
	
	int encodeDouble(BitWriter writer, double value) {
		return EliasOmegaEncoding.encodeDouble(value, writer);
	}
	
	double decodeDouble(BitReader reader) {
		return EliasOmegaEncoding.decodeDouble(reader);
	}

	@Override
	public String toString() {
		return stats.toString();
	}
}
