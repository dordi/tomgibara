package com.tomgibara.crinch.perm;

public interface Permutable {
	
	Permutable transpose(int i, int j);
	
	int getPermutableSize();
	
}