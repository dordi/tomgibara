package com.tomgibara.crinch.perm;

public interface Permutable {
	
	Permutable swap(int i, int j);
	
	int getPermutableSize();
	
}