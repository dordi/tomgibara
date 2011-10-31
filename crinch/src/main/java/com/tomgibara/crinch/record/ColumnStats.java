package com.tomgibara.crinch.record;

import java.math.BigDecimal;
import java.util.Arrays;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.EliasOmegaEncoding;

//TODO add enumerations array
public class ColumnStats {

	public enum Classification {
		ENUMERATED,
		TEXTUAL,
		INTEGRAL,
		FLOATING
	}

	private static int writeDecimal(BigDecimal value, BitWriter writer) {
		int c = writer.writeBoolean(value == null);
		if (value != null) c+= EliasOmegaEncoding.encodeDecimal(value, writer);
		return c;
	}
	
	private static BigDecimal readDecimal(BitReader reader) {
		return reader.readBoolean() ? null : EliasOmegaEncoding.decodeDecimal(reader);
	}
	
	private static int writeString(String str, BitWriter writer) {
		int length = str.length();
		int c = EliasOmegaEncoding.encodePositiveInt(length + 1, writer);
		for (int i = 0; i < length; i++) {
			c += EliasOmegaEncoding.encodePositiveInt(str.charAt(i) + 1, writer);
		}
		return c;
	}
	
	private static String readString(BitReader reader) {
		int length = EliasOmegaEncoding.decodePositiveInt(reader) - 1;
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append((char) (EliasOmegaEncoding.decodePositiveInt(reader) - 1));
		}
		return sb.toString();
	}
	
	//TODO not entirely correct: huffman implementation may vary and produce different codes
	public static int write(ColumnStats stats, BitWriter writer) {
		int c = EliasOmegaEncoding.encodePositiveInt(stats.classification.ordinal() + 1, writer);
		c += writer.writeBoolean(stats.nullable);
		c += writeDecimal(stats.maximum, writer);
		c += writeDecimal(stats.minimum, writer);
		c += writeDecimal(stats.sum, writer);
		c += EliasOmegaEncoding.encodePositiveLong(stats.count + 1, writer);
		final long[] freqs = stats.frequencies;
		int freqCount = freqs == null ? 0 : freqs.length;
		c += EliasOmegaEncoding.encodePositiveInt(freqCount + 1, writer);
		for (int i = 0; i < freqCount; i++) {
			c += EliasOmegaEncoding.encodePositiveLong(freqs[i] + 1, writer);
		}
		final String[] enums = stats.enumeration;
		int enumCount = enums == null ? 0 : enums.length;
		c += EliasOmegaEncoding.encodePositiveInt(enumCount + 1, writer);
		for (int i = 0; i < enumCount; i++) {
			c += writeString(enums[i], writer);
		}
		return c;
	}
	
	public static ColumnStats read(BitReader reader) {
		ColumnStats stats = new ColumnStats();
		stats.setClassification(ColumnStats.Classification.values()[EliasOmegaEncoding.decodePositiveInt(reader) - 1]);
		stats.setNullable(reader.readBoolean());
		stats.setMaximum(readDecimal(reader));
		stats.setMinimum(readDecimal(reader));
		stats.setSum(readDecimal(reader));
		stats.setCount(EliasOmegaEncoding.decodePositiveLong(reader) - 1);
		
		int freqCount = EliasOmegaEncoding.decodePositiveInt(reader) - 1;
		long[] freqs;
		if (freqCount == 0) {
			freqs = null;
		} else {
			freqs = new long[freqCount];
			for (int i = 0; i < freqCount; i++) {
				freqs[i] = EliasOmegaEncoding.decodePositiveLong(reader) - 1;
			}
		}
		stats.setFrequencies(freqs);
		
		int enumCount = EliasOmegaEncoding.decodePositiveInt(reader) - 1;
		String[] enums;
		if (enumCount == 0) {
			enums = null;
		} else {
			enums = new String[enumCount];
			for (int i = 0; i < enumCount; i++) {
				enums[i] = readString(reader);
			}
		}
		stats.setEnumeration(enums);
		
		return stats;
	}
	

	private Classification classification;
	private boolean nullable;
	private BigDecimal minimum;
	private BigDecimal maximum;
	private BigDecimal sum;
	private long count;
	private long[] frequencies;
	private String[] enumeration;

	public void setClassification(Classification classification) {
		this.classification = classification;
	}
	
	public Classification getClassification() {
		return classification;
	}
	
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public boolean isNullable() {
		return nullable;
	}
	
	public void setMinimum(BigDecimal minimum) {
		this.minimum = minimum;
	}
	
	public BigDecimal getMinimum() {
		return minimum;
	}
	
	public void setMaximum(BigDecimal maximum) {
		this.maximum = maximum;
	}
	
	public BigDecimal getMaximum() {
		return maximum;
	}
	
	public void setSum(BigDecimal sum) {
		this.sum = sum;
	}
	
	public BigDecimal getSum() {
		return sum;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setFrequencies(long[] frequencies) {
		if (frequencies != null && frequencies.length == 0) frequencies = null;
		this.frequencies = frequencies;
	}

	public long[] getFrequencies() {
		return frequencies;
	}
	
	public void setEnumeration(String[] enumeration) {
		if (enumeration != null && enumeration.length == 0) enumeration = null;
		this.enumeration = enumeration;
	}
	
	public String[] getEnumeration() {
		return enumeration;
	}
	
	@Override
	public String toString() {
		return "classification: " + classification + ", nullable? " + nullable + ", minimum: " + minimum + ", maximum: " + maximum + " sum: " + sum + ", count: " + count + ", frequencies: " + Arrays.toString(frequencies) + ", enumeration: " + Arrays.toString(enumeration);
	}
	
}
