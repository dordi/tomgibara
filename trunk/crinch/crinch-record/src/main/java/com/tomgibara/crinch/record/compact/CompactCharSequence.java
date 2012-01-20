package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.record.ProcessScoped;

class CompactCharSequence implements CharSequence, Comparable<CharSequence>, ProcessScoped {

	final CompactCharStore store;
	final int columnIndex;
	final int capacity;
	
	private final char[] chars;
	private int length;
	private String string = null;
	
	CompactCharSequence next; // used for forming linked list caches
	
	CompactCharSequence(CompactCharStore store, int columnIndex, int capacity) {
		this.store = store;
		this.columnIndex = columnIndex;
		this.capacity = capacity;
		
		chars = new char[capacity];
		length = 0;
	}

	void append(char c) {
		chars[length++] = c;
		string = null;
	}

	void recycle() {
		length = 0;
		string = null;
		next = null;
		store.storeChars(this);
	}
	
	@Override
	public int length() {
		return length;
	}
	
	@Override
	public char charAt(int index) {
		if (index >= length) throw new StringIndexOutOfBoundsException();
		return chars[index];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (start < 0) throw new IndexOutOfBoundsException("negative start");
		if (end > length) throw new IndexOutOfBoundsException("end exceeds length");
		if (start > end) throw new IndexOutOfBoundsException("start after end");
		return new String(chars, start, end - start);
	}
	
	@Override
	public int compareTo(CharSequence seq) {
		if (seq instanceof String) return toString().compareTo((String) seq);
		if (seq instanceof CompactCharSequence) {
			CompactCharSequence that = (CompactCharSequence) seq;
			if (this == that) return 0;
			int len = Math.min(this.length, that.length);
			char[] chars1 = this.chars;
			char[] chars2 = that.chars;
			for (int i = 0; i < len; i++) {
				char c1 = chars1[i];
				char c2 = chars2[i];
				if (c1 != c2) return c1 - c2;
			}
			return this.length - that.length;
		}
		int len = Math.min(this.length, seq.length());
		char[] chars = this.chars;
		for (int i = 0; i < len; i++) {
			char c1 = chars[i];
			char c2 = seq.charAt(i);
			if (c1 != c2) return c1 - c2;
		}
		return this.length - seq.length();
	}
	
	@Override
	public String toString() {
		String str = string;
		if (str == null) {
			str = new String(chars, 0, length);
			string = str;
		}
		return str;
	}
	
}
