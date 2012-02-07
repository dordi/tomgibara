package com.tomgibara.crinch.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import com.tomgibara.crinch.bits.BitVector;

// a set containing ints over a small range
public class RangedIntSet extends AbstractSet<Integer> implements SortedSet<Integer> {

	private final BitVector bits;
	private final int offset; // the value that must be added to received values to map them onto the bits

	// constructors
	
	private RangedIntSet(BitVector bits, int offset) {
		this.bits = bits;
		this.offset = offset;
	}
	
	public RangedIntSet(int from, int to) {
		if (from > to) throw new IllegalArgumentException("from exceeds to");
		bits = new BitVector(to - from);
		offset = -from;
	}

	public RangedIntSet(int to) {
		this(0, to);
	}

	// set methods
	
	@Override
	public int size() {
		return bits.countOnes();
	}
	
	@Override
	public boolean isEmpty() {
		return bits.isAllZeros();
	}
	
	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Integer)) return false;
		int value = offset + (Integer) o;
		return value >= 0 && value < bits.size() && bits.getBit(value);
	}

	@Override
	public boolean add(Integer e) {
		return !bits.getAndSetBit(value(e), true);
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Integer)) return false;
		int i = offset + (Integer) o;
		if (i < 0 || i >= bits.size()) return false;
		return bits.getAndSetBit(i, false);
	}
	
	@Override
	public void clear() {
		bits.set(false);
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			final Iterator<Integer> it = bits.positionIterator();
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			
			@Override
			public Integer next() {
				return it.next() - offset;
			}
			
			@Override
			public void remove() {
				it.remove();
			}
			
		};
	}

	@Override
	public boolean containsAll(Collection<?> c) {
//		if (c instanceof CompactIntSet) {
//			CompactIntSet that = (CompactIntSet) c;
//			if (c == this) return true;
//			int thisSize = this.size();
//			int thatSize = that.size();
//			if (thisSize == thatSize) return this.bits.compare(Comparison.CONTAINS, that.bits);
//			if (thisSize > thatSize) return this.bits.rangeView(0, thatSize).compare(Comparison.CONTAINS, that.bits);
//			if (!that.bits.isRangeAllZeros(thisSize, thatSize)) return false;
//			return this.bits.compare(Comparison.CONTAINS, that.bits.rangeView(0, thisSize));
//		}
		
		return super.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends Integer> c) {
//		if (c instanceof CompactIntSet) {
//			CompactIntSet that = (CompactIntSet) c;
//			if (c == this) return false;
//			int thisSize = this.size();
//			int thatSize = that.size();
//			BitVector orig = bits.copy();
//			if (thisSize == thatSize) {
//				this.bits.andVector(that.bits);
//			} else if (thisSize > thatSize) {
//				this.bits.andVector(0, that.bits);
//			} else {
//				this.bits.andVector(that.bits.rangeView(0, thisSize));
//			}
//			return !bits.equals(orig);
//		}
		
		for (Integer e : c) value(e);
		Iterator<? extends Integer> it = c.iterator();
		boolean changed = false;
		while (!changed && it.hasNext()) {
			changed = !bits.getAndSetBit(it.next() + offset, true);
		}
		while (it.hasNext()) {
			bits.setBit(it.next() + offset, true);
		}
		return changed;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
//		if (c instanceof CompactIntSet) {
//			CompactIntSet that = (CompactIntSet) c;
//			if (this == that) {
//				boolean changed = !bits.isAllZeros();
//				if (changed) bits.set(false);
//				return changed;
//			}
//			int thisSize = this.size();
//			int thatSize = that.size();
//			if (thisSize == thatSize) {
//				boolean changed = this.bits.compare(Comparison.INTERSECTS, that.bits);
//				if (changed) {
//					BitVector copy = that.bits.mutableCopy();
//					copy.flip();
//					this.bits.andVector(copy);
//				}
//				return changed;
//			}
//			if (thisSize > thatSize) {
//				boolean changed = this.bits.rangeView(0, thatSize).compare(Comparison.INTERSECTS, that.bits);
//				if (changed) {
//					BitVector copy = that.bits.mutableCopy();
//					copy.flip();
//					this.bits.andVector(0, copy);
//				}
//				return changed;
//			}
//			boolean changed = this.bits.compare(Comparison.INTERSECTS, that.bits.rangeView(0, thisSize));
//			if (changed) {
//				BitVector copy = that.bits.mutableRangeCopy(0, thisSize);
//				copy.flip();
//				this.bits.andVector(copy);
//			}
//			return changed;
//		}

		return super.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
//		if (c instanceof CompactIntSet) {
//			CompactIntSet that = (CompactIntSet) c;
//			if (c == this) return false;
//			int thisSize = this.size();
//			int thatSize = that.size();
//			if (thisSize == thatSize) {
//				boolean changed = !that.bits.compare(Comparison.CONTAINS, this.bits);
//				if (changed) this.bits.andVector(that.bits);
//				return changed;
//			}
//			if (thisSize > thatSize) {
//				boolean changed = !that.bits.compare(Comparison.CONTAINS, this.bits.rangeView(0, thatSize));
//				if (changed) {
//					this.bits.andVector(0, that.bits);
//					this.bits.setRange(thatSize, thisSize, false);
//					return true;
//				}
//				changed = !this.bits.isRangeAllZeros(thatSize, thisSize);
//				if (changed) {
//					this.bits.setRange(thatSize, thisSize, false);
//					return true;
//				}
//				return false;
//			}
//			BitVector view = that.bits.rangeView(0, thisSize);
//			boolean changed = !view.compare(Comparison.CONTAINS, this.bits);
//			if (changed) this.bits.andVector(view);
//			return changed;
//		}
		
		return super.retainAll(c);
	}
	
	// sorted set methods
	
	@Override
	public Comparator<? super Integer> comparator() {
		return null;
	}

	@Override
	public Integer first() {
		int i = bits.firstOne();
		if (i == bits.size()) throw new NoSuchElementException();
		return i - offset;
	}
	
	@Override
	public Integer last() {
		int i = bits.lastOne();
		if (i == -1) throw new NoSuchElementException();
		return i - offset;
	}
	
	@Override
	public SortedSet<Integer> headSet(Integer toElement) {
		if (toElement == null) throw new NullPointerException();
		int upper = Math.max(toElement + offset, 0);
		return new RangedIntSet(this.bits.rangeView(0, upper), offset);
	}
	
	@Override
	public SortedSet<Integer> tailSet(Integer fromElement) {
		if (fromElement == null) throw new NullPointerException();
		int lower = Math.min(fromElement + offset, bits.size());
		return new RangedIntSet(this.bits.rangeView(lower, bits.size()), offset - lower);
	}
	
	@Override
	public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
		if (fromElement == null) throw new NullPointerException();
		if (toElement == null) throw new NullPointerException();
		int from = fromElement;
		int to = toElement;
		if (from > to) throw new IllegalArgumentException("from exceeds to");
		int lower = Math.min(from + offset, bits.size());
		int upper = Math.max(to + offset, 0);
		return new RangedIntSet(this.bits.rangeView(lower, upper), offset - lower);
	}
	
	// object methods
	
	@Override
	public boolean equals(Object o) {
//		if (o instanceof RangedIntSet) {
//			RangedIntSet that = (RangedIntSet) o;
//			if (o == this) return false;
//			int thisSize = this.size();
//			int thatSize = that.size();
//			if (thisSize == thatSize) return this.bits.compare(Comparison.EQUALS, that.bits);
//			if (thisSize > thatSize) return this.bits.rangeView(0, thatSize).compare(Comparison.EQUALS, that.bits) && this.bits.isRangeAllZeros(thatSize, thisSize);
//			/*if (thisSize < thatSize)*/ return this.bits.compare(Comparison.EQUALS, that.bits.rangeView(0, thisSize)) && that.bits.isRangeAllOnes(thisSize, thatSize);
//		}
		return super.equals(o);
	}

	// private methods
	
	private int value(Integer e) {
		if (e == null) throw new NullPointerException("null value");
		int i = e + offset;
		if (i < 0) throw new IllegalArgumentException("value less than lower bound");
		if (i >= bits.size()) throw new IllegalArgumentException("value greater than upper bound");
		return i;
	}
	
}
