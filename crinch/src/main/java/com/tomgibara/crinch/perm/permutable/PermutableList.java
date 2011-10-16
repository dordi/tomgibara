package com.tomgibara.crinch.perm.permutable;

import java.util.List;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableList<E> implements Permutable {

	private final List<E> list;
	
	public PermutableList(List<E> list) {
		if (list == null) throw new IllegalArgumentException("null list");
		this.list = list;
	}
	
	public List<E> getList() {
		return list;
	}
	
	@Override
	public int getPermutableSize() {
		return list.size();
	}
	
	@Override
	public PermutableList<E> transpose(int i, int j) {
		E e = list.get(i);
		list.set(i, list.get(j));
		list.set(j, e);
		return this;
	}
	
}
