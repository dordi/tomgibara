package com.tomgibara.money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoneySplitter {

	// statics

	private static final Partition FREE = new Partition();
	
	public static BigDecimal[] toBigDecimalArray(Long... longs) {
		if (longs == null) throw new IllegalArgumentException("null longs");
		BigDecimal[] decimals = new BigDecimal[longs.length];
		for (int i = 0; i < longs.length; i++) {
			Long l = longs[i];
			decimals[i] = l == null ? null : BigDecimal.valueOf(l);
		}
		return decimals;
	}
	
	public static BigDecimal[] toBigDecimalArray(Double... doubles) {
		if (doubles == null) throw new IllegalArgumentException("null doubles");
		BigDecimal[] decimals = new BigDecimal[doubles.length];
		for (int i = 0; i < doubles.length; i++) {
			Double d = doubles[i];
			decimals[i] = d == null ? null : BigDecimal.valueOf(d);
		}
		return decimals;
	}
	
	// fields
	
	private final MoneyCalc calc;
	private List<Partition> partitions = null;
	private int parts = 0;

	// constructors
	
	MoneySplitter(MoneyCalc calc) {
		this.calc = calc;
	}
	
	// accessors
	
	public int getParts() {
		return parts;
	}
	
	public MoneySplitter setParts(int parts) {
		if (parts < 0) throw new IllegalArgumentException("negative parts");
		this.parts = parts;
		return this;
	}
	
	public MoneySplitter setProportions(BigDecimal... proportions) {
		for (int i = 0; i < proportions.length; i++) {
			getPartition(i).proportion = proportions[i];
		}
		return growParts(proportions.length);
	}

	public MoneySplitter setMinima(BigDecimal... minima) {
		for (int i = 0; i < minima.length; i++) {
			getPartition(i).minimum = minima[i];
		}
		return growParts(minima.length);
	}
	
	public MoneySplitter setMaxima(BigDecimal... maxima) {
		for (int i = 0; i < maxima.length; i++) {
			getPartition(i).maximum = maxima[i];
		}
		return growParts(maxima.length);
	}
	
	public MoneySplitter setProportion(int index, BigDecimal proportion) {
		getPartition(index).proportion = proportion;
		return growParts(index + 1);
	}
	
	public MoneySplitter setMinimum(int index, BigDecimal minimum) {
		getPartition(index).minimum = minimum;
		return growParts(index + 1);
	}
	
	public MoneySplitter setMaximum(int index, BigDecimal maximum) {
		getPartition(index).maximum = maximum;
		return growParts(index + 1);
	}
	
	// methods

	public Money[] split() {
		if (isConstrained()) {
			throw new UnsupportedOperationException();
		} else {
			return splitFree();
		}
	}
	
	// private utility methods
	
	private Money[] splitFree() {
		if (calc.scale < 0) throw new IllegalStateException("no scale set");
		if (parts < 1) throw new IllegalArgumentException("parts not positive");
		Money[] monies = new Money[parts];
		BigDecimal remainder = calc.getAmount();
		for (int i = parts - 1; i >= 0; i--) {
			if (i == 0) {
				monies[i] = new Money(calc.getType(), remainder);
			} else {
				BigDecimal share = remainder.divide(BigDecimal.valueOf(i + 1), calc.scale, calc.roundingMode);
				monies[i] = new Money(calc.getType(), share);
				remainder = remainder.subtract(share);
			}
		}
		return monies;

	}
	
	private Partition getPartition(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (partitions == null) partitions = new ArrayList<Partition>();
		Partition partition = null;
		while (partitions.size() <= index) {
			partition = new Partition();
			partitions.add(partition);
		}
		return partition == null ? partitions.get(index) : partition;
	}

	private MoneySplitter growParts(int size) {
		return size <= parts ? this : setParts(size);
	}
	
	private void checkParts() {
		if (parts == 0) throw new IllegalStateException("zero parts");
	}

	private boolean isConstrained() {
		if (partitions == null) return false;
		int limit = Math.min(partitions.size(), parts);
		for (int i = 0; i < limit; i++) {
			if (!getEffectivePartition(i).isUnconstrained()) return false;
		}
		return true;
	}
	
	private Partition getEffectivePartition(int index) {
		if (partitions == null) return FREE;
		Partition partition = partitions.get(index);
		return partition == null ? null : FREE;
	}
	
	// inner classes
	
	private static class Partition {
		
		BigDecimal minimum;
		BigDecimal maximum;
		BigDecimal proportion;
		
		boolean isUnconstrained() {
			return minimum == null & maximum == null & proportion == null;
		}
		
	}
	
}
