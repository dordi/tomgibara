package com.tomgibara.money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoneySplitter {

	// statics

	private static final Partition FREE = new Partition(-1);

	private static Money[] splitFree(MoneyCalc calc, BigDecimal amount, int parts) {
		Money[] monies = new Money[parts];
		BigDecimal remainder = amount;
		for (int i = parts - 1; i >= 0; i--) {
			if (i == 0) {
				monies[i] = calc.money(remainder);
			} else {
				BigDecimal share = remainder.divide(BigDecimal.valueOf(i + 1), calc.scale, calc.roundingMode);
				monies[i] = calc.money(share);
				remainder = remainder.subtract(share);
			}
		}
		return monies;
	}
	

	
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
	private boolean boundedAbove = true;

	// constructors
	
	MoneySplitter(MoneyCalc calc) {
		this.calc = calc;
	}
	
	// accessors
	
	public int getParts() {
		return parts;
	}

	public void setBoundedAbove(boolean boundedAbove) {
		this.boundedAbove = boundedAbove;
	}
	
	public boolean isBoundedAbove() {
		return boundedAbove;
	}
	
	public MoneySplitter setParts(int parts) {
		if (parts < 0) throw new IllegalArgumentException("negative parts");
		this.parts = parts;
		return this;
	}
	
	public MoneySplitter setProportions(BigDecimal... proportions) {
		for (int i = 0; i < proportions.length; i++) {
			BigDecimal proportion = proportions[i];
			if (proportion.signum() < 0) throw new IllegalArgumentException("negative proportion");
			getPartition(i).proportion = proportion;
		}
		return growParts(proportions.length);
	}

	public MoneySplitter setBounds(BigDecimal... bounds) {
		for (int i = 0; i < bounds.length; i++) {
			getPartition(i).bound = bounds[i];
		}
		return growParts(bounds.length);
	}
	
	public MoneySplitter setProportion(int index, BigDecimal proportion) {
		if (proportion.signum() < 0) throw new IllegalArgumentException("negative proportion");
		getPartition(index).proportion = proportion;
		return growParts(index + 1);
	}
	
	public MoneySplitter setBound(int index, BigDecimal bound) {
		getPartition(index).bound = bound;
		return growParts(index + 1);
	}
	
	// methods

	public Money[] split() {
		checkParts();
		boolean bounded = isBounded();
		boolean proportioned = isProportioned();
		if (!bounded && !proportioned) return splitFree();
		if (!bounded) return splitProportioned();
		if (!proportioned) return splitBounded();
		throw new UnsupportedOperationException();
	}
	
	// private utility methods
	
	private Money[] splitFree() {
		return splitFree(calc, calc.getAmount(), parts);
	}
	
	private Money[] splitProportioned() {
		// trivial case
		if (parts == 1) {
			if (getEffectiveProportion(0).signum() == 0) throw new IllegalStateException("all proportions zero");
			return new Money[] { calc.money() };
		}
		
		BigDecimal[] denominators = new BigDecimal[parts];
		{
			BigDecimal denominator = null;
			for (int i = 0; i < parts; i++) {
				BigDecimal proportion = getEffectiveProportion(i);
				denominator = i == 0 ? proportion : denominator.add(proportion);
				denominators[i] = denominator;
			}
			if (denominator.signum() == 0) throw new IllegalArgumentException("all proportions zero");
		}
		Money[] monies = new Money[parts];
		BigDecimal remainder = calc.getAmount();
		Money none = null; // lazily instantiated
		for (int i = parts - 1; i >= 0; i--) {
			if (i == 0) {
				monies[i] = calc.money(remainder);
			} else {
				BigDecimal proportion = getEffectiveProportion(i);
				if (proportion.signum() == 0) {
					if (none == null) none = calc.money(BigDecimal.ZERO);
					monies[i] = none;
				} else {
					BigDecimal share = remainder.multiply(proportion).divide(denominators[i], calc.scale, calc.roundingMode);
					monies[i] = calc.money(share);
					remainder = remainder.subtract(share);
				}
			}
		}
		return monies;
	}
	
	private Money[] splitBounded() {
		throw new UnsupportedOperationException();
	}
	
	private Partition getPartition(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (partitions == null) partitions = new ArrayList<Partition>();
		Partition partition = null;
		while (partitions.size() <= index) {
			partition = new Partition(index);
			partitions.add(partition);
		}
		return partition == null ? partitions.get(index) : partition;
	}

//	private Partition[] getConstrainedPartitions() {
//		if (partitions == null) return new Partition[0];
//		List<Partition> partitions = new ArrayList<Partition>();
//		int limit = Math.min(partitions.size(), parts);
//		for (int i = 0; i < limit; i++) {
//			Partition partition = getEffectivePartition(i);
//			if (partition.isConstrained()) {
//				partitions.add(partition);
//			}
//		}
//		Partition[] array = (Partition[]) partitions.toArray(new Partition[partitions.size()]);
//		Arrays.sort(array);
//		return array;
//	}
	
	private MoneySplitter growParts(int size) {
		return size <= parts ? this : setParts(size);
	}
	
	private void checkParts() {
		if (parts == 0) throw new IllegalStateException("zero parts");
	}

	private boolean isBounded() {
		if (partitions != null) {
			int limit = Math.min(partitions.size(), parts);
			for (int i = 0; i < limit; i++) {
				if (getEffectivePartition(i).isBounded()) return true;
			}
		}
		return false;
	}
	
	private boolean isProportioned() {
		if (partitions != null) {
			int limit = Math.min(partitions.size(), parts);
			for (int i = 0; i < limit; i++) {
				if (getEffectivePartition(i).isProportioned()) return true;
			}
		}
		return false;
	}
	
//	private int countConstraints() {
//		int count = 0;
//		if (partitions != null) {
//			int limit = Math.min(partitions.size(), parts);
//			for (int i = 0; i < limit; i++) {
//				if (getEffectivePartition(i).isConstrained()) count++;
//			}
//		}
//		return count;
//	}
	
	private BigDecimal getEffectiveProportion(int index) {
		Partition partition = getEffectivePartition(index);
		BigDecimal proportion = partition.proportion;
		return proportion == null ? BigDecimal.ZERO : proportion;
	}
	
	private Partition getEffectivePartition(int index) {
		if (partitions == null) return FREE;
		Partition partition = partitions.get(index);
		return partition == null ? FREE : partition;
	}
	
	// inner classes
	
	private static class Partition implements Comparable<Partition> {
		
		final int index;
		
		BigDecimal bound;
		BigDecimal proportion;
		
		public Partition(int index) {
			this.index = index;
		}
		
		boolean isBounded() {
			return bound != null;
		}
		
		boolean isProportioned() {
			return proportion != null;
		}
		
		@Override
		public int compareTo(Partition that) {
			if (this == that) return 0;
			throw new UnsupportedOperationException();
		}
		
	}
	
}
