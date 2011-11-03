package com.tomgibara.crinch.bits;

public enum BitBoundary {

	BYTE(3),
	SHORT(4),
	INT(5),
	LONG(6);
	
	final int scale;
	final int mask;
	
	private BitBoundary(int scale) {
		this.scale = scale;
		this.mask = (1 << scale) - 1;
	}
	
}