package com.tomgibara.crinch.perm;

public class PermutableString implements Permutable {

	private final StringBuilder sb;
	
	public PermutableString(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		sb = new StringBuilder(str);
	}
	
	public PermutableString(StringBuilder sb) {
		if (sb == null) throw new IllegalArgumentException("null sb");
		this.sb = sb;
	}
	
	
	public StringBuilder getStringBuilder() {
		return sb;
	}
	
	@Override
	public int getPermutableSize() {
		return sb.length();
	}
	
	@Override
	public Permutable transpose(int i, int j) {
		char c = sb.charAt(i);
		sb.setCharAt(i, sb.charAt(j));
		sb.setCharAt(j, c);
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
