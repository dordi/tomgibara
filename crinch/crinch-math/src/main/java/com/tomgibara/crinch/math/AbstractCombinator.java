package com.tomgibara.crinch.math;

abstract class AbstractCombinator implements Combinator {

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Combinator)) return false;
		Combinator that = (Combinator) obj;
		if (this.getElementCount() != that.getElementCount()) return false;
		if (this.getTupleLength() != that.getTupleLength()) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return getElementCount() ^ (getTupleLength() * 31);
	}
	
	@Override
	public String toString() {
		return getElementCount() + " choose " + getTupleLength();
	}
}
