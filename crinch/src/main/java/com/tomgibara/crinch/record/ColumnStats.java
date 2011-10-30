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
			for (int i = 0; i < freqs.length; i++) {
				freqs[i] = EliasOmegaEncoding.decodePositiveLong(reader) - 1;
			}
		}
		stats.setFrequencies(freqs);
		return stats;
	}
	

	private Classification classification;
	private boolean nullable;
	private BigDecimal minimum;
	private BigDecimal maximum;
	private BigDecimal sum;
	private long count;
	private long[] frequencies;

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
	
	public long[] getFrequencies() {
		return frequencies;
	}
	
	public void setFrequencies(long[] frequencies) {
		if (frequencies != null && frequencies.length == 0) frequencies = null;
		this.frequencies = frequencies;
	}
	
	@Override
	public String toString() {
		return "classification: " + classification + ", nullable? " + nullable + ", minimum: " + minimum + ", maximum: " + maximum + " sum: " + sum + ", count: " + count + ", frequencies: " + Arrays.toString(frequencies);
	}
	
}
