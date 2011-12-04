/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record.def;

import java.util.Comparator;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.Hashes;
import com.tomgibara.crinch.util.WriteStream;

public class ColumnOrder {

	// statics
	
	static Comparator<ColumnOrder> comparator = new Comparator<ColumnOrder>() {
		
		@Override
		public int compare(ColumnOrder a, ColumnOrder b) {
			if (a.precedence != b.precedence) return a.precedence - b.precedence;
			if (a.ascending != b.ascending) return a.ascending ? -1 : 1;
			if (a.nullFirst != b.nullFirst) return a.nullFirst ? -1 : 1;
			return 0;
		}
	};

	static Comparator<ColumnDef> columnComparator = new Comparator<ColumnDef>() {
		
		public int compare(ColumnDef a, ColumnDef b) {
			ColumnOrder aOrd = a.getOrder();
			ColumnOrder bOrd = b.getOrder();
			if (aOrd == null || bOrd == null) throw new IllegalArgumentException("missing order");
			int c = comparator.compare(aOrd, bOrd);
			return c == 0 ? a.getIndex() - b.getIndex() : c;
		}
		
	};

	static HashSource<ColumnOrder> hashSource = new HashSource<ColumnOrder>() {
		
		@Override
		public void sourceData(ColumnOrder order, WriteStream out) {
			out.writeInt(order.precedence);
			out.writeBoolean(order.ascending);
			out.writeBoolean(order.nullFirst);
		}
	};

	// fields
	
	private final int precedence;
	private final boolean ascending;
	private final boolean nullFirst;
	
	// constructors
	
	public ColumnOrder(int precedence, boolean ascending, boolean nullFirst) {
		if (precedence < 0) throw new IllegalArgumentException("negative precedence");
		this.precedence = precedence;
		this.ascending = ascending;
		this.nullFirst = nullFirst;
	}
	
	// accessors
	
	public int getPrecedence() {
		return precedence;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public boolean isNullFirst() {
		return nullFirst;
	}
	
	// package scoped methods
	
	ColumnOrder withPrecedence(int precedence) {
		return precedence == this.precedence ? this : new ColumnOrder(precedence, ascending, nullFirst);
	}
	
	// object methods

	@Override
	public int hashCode() {
		return this.precedence ^ Hashes.hashCode(ascending) ^ (31 * Hashes.hashCode(nullFirst));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof ColumnOrder)) return false;
		ColumnOrder that = (ColumnOrder) obj;
		if (this.precedence != that.precedence) return false;
		if (this.ascending != that.ascending) return false;
		if (this.nullFirst != that.nullFirst) return false;
		return true;
	}

	@Override
	public String toString() {
		return "[precedence: " + precedence + ", ascending: " + ascending + ", nullFirst: " + nullFirst + "]";
	}
	
	// inner classes
	
	public static class Indexed {
		
		private final int index;
		private final ColumnOrder order;
		
		public Indexed(int index, ColumnOrder order) {
			if (index < 0) throw new IllegalArgumentException("negative index");
			if (order == null) throw new IllegalArgumentException("null order");
			
			this.index = index;
			this.order = order;
		}
		
		public int getIndex() {
			return index;
		}
		
		public ColumnOrder getOrder() {
			return order;
		}
		
		// TODO object methods
		
	}
	
}