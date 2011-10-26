package com.tomgibara.crinch.record.compact;

import java.util.Arrays;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.EliasOmegaEncoding;
import com.tomgibara.crinch.coding.Huffman;

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
	
	//TODO not entirely correct: huffman implementation may vary and produce different codes
	static int write(ColumnCompactor cc, BitWriter writer) {
		int c = EliasOmegaEncoding.encode(cc.type.ordinal() + 1, writer);
		c += writer.writeBoolean(cc.nullable);
		//TODO needs coding support for signed long values
		//c += writer.write(cc.offset, 64);
		c += writer.write((int) cc.offset, 32);
		int freqCount = cc.freqs == null ? 0 : cc.freqs.length;
		c += EliasOmegaEncoding.encode(freqCount + 1, writer);
		for (int i = 0; i < freqCount; i++) {
			c += EliasOmegaEncoding.encodeLong(cc.freqs[i] + 1, writer);
		}
		return c;
	}
	
	static ColumnCompactor read(BitReader reader) {
		int typeOrdinal = EliasOmegaEncoding.decode(reader) - 1;
		ColumnType type = ColumnType.values()[typeOrdinal];
		boolean nullable = reader.readBoolean();
		//long offset = reader.readLong(64);
		long offset = reader.read(32);
		int freqCount = EliasOmegaEncoding.decode(reader) - 1;
		long[] freqs;
		if (freqCount == 0) {
			freqs = null;
		} else {
			freqs = new long[freqCount];
			for (int i = 0; i < freqs.length; i++) {
				freqs[i] = EliasOmegaEncoding.decodeLong(reader) - 1;
			}
		}
		return new ColumnCompactor(type, nullable, offset, freqs);
	}
	

	private final ColumnType type;
	private final boolean nullable;
	private final long offset;
	private final long[] freqs;
	
	private final int[][] lookup;
	private final Huffman huffman;
	
	ColumnCompactor(ColumnType type, boolean nullable, long offset, long[] freqs) {
		if (freqs != null && freqs.length == 0) freqs = null;
		this.type = type;
		this.nullable = nullable;
		this.offset = offset;
		this.freqs = freqs;
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

	public ColumnType getType() {
		return type;
	}
	
	boolean isNullable() {
		return nullable;
	}

	int encodeString(BitWriter writer, String value) {
		int length = value.length();
		int n = EliasOmegaEncoding.encodeSigned(length - (int) offset, writer);
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			n += huffman.encode(lookup[1][c]+1, writer);
		}
		return n;
	}
	
	int encodeNull(BitWriter writer, boolean isNull) {
		if (!nullable) return 0;
		return writer.writeBoolean(isNull);
	}
	
	boolean decodeNull(BitReader reader) {
		if (!nullable) return false;
		return reader.readBoolean();
	}
	
	String decodeString(BitReader reader) {
		int length = ((int) offset) + EliasOmegaEncoding.decodeSigned(reader);
		StringBuilder sb = new StringBuilder(length);
		for (; length > 0; length--) {
			sb.append( (char) lookup[0][huffman.decode(reader)-1] );
		}
		return sb.toString();
	}
	
	int encodeInt(BitWriter writer, int value) {
		return EliasOmegaEncoding.encodeSigned(value - (int) offset, writer);
	}
	
	int decodeInt(BitReader reader) {
		return ((int) offset) + EliasOmegaEncoding.decodeSigned(reader);
	}
	
	// TODO not fully implemented
	int encodeLong(BitWriter writer, long value) {
		return EliasOmegaEncoding.encodeLong(value - offset, writer);
	}
	
	long decodeLong(BitReader reader) {
		return offset + EliasOmegaEncoding.decodeLong(reader);
	}
	
	int encodeBoolean(BitWriter writer, boolean value) {
		return writer.writeBoolean(value);
	}
	
	boolean decodeBoolean(BitReader reader) {
		return reader.readBoolean();
	}
	
	int encodeFloat(BitWriter writer, float value) {
		return writer.write(Float.floatToIntBits(value), 32);
	}
	
	float decodeFloat(BitReader reader) {
		return Float.intBitsToFloat( reader.read(32) );
	}
	
	int encodeDouble(BitWriter writer, double value) {
		return writer.write(Double.doubleToLongBits(value), 64);
	}
	
	double decodeDouble(BitReader reader) {
		return Double.longBitsToDouble( reader.readLong(64) );
	}

	@Override
	public String toString() {
		return "Nullable? " + nullable + " Offset: " + offset + " Freqs: " + Arrays.toString(freqs);
	}
}
