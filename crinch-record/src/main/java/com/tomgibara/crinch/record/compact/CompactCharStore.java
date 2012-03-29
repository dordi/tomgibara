package com.tomgibara.crinch.record.compact;

class CompactCharStore {

	private CompactCharSequence[] store;
	
	void setCharColumns(int columns) {
		store = new CompactCharSequence[columns];
	}
	
	void storeChars(CompactCharSequence cs) {
		int columnIndex = cs.columnIndex;
		CompactCharSequence stored = store[columnIndex];
		if (stored == null || stored.capacity < cs.capacity) {
			store[columnIndex] = cs;
		}
	}
	
	CompactCharSequence getChars(int columnIndex, int capacity) {
		CompactCharSequence stored = store[columnIndex];
		if (stored == null || stored.capacity < capacity) return new CompactCharSequence(this, columnIndex, capacity);
		store[columnIndex] = null;
		return stored;
	}
	
}
