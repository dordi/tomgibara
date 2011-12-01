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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.record.def.ColumnOrder.Indexed;
import com.tomgibara.crinch.util.WriteStream;

public class RecordDefinition {
	
	// statics
	
	public static class Builder {
		
		private final RecordDefinition basis;

		private boolean ordinal;
		private boolean positional;
		private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		private int index = -1;
		private ColumnType type;
		private ColumnOrder order;
		
		Builder(RecordDefinition basis) {
			this.basis = basis;
			if (basis == null) {
				ordinal = true;
				positional = true;
			} else {
				ordinal = basis.ordinal;
				positional = basis.positional;
			}
		}
		
		public Builder setOrdinal(boolean ordinal) {
			if (basis != null && !basis.ordinal) throw new IllegalStateException("basis has no ordinals");
			this.ordinal = ordinal;
			return this;
		}
		
		public Builder setPositional(boolean positional) {
			if (basis != null && !basis.positional) throw new IllegalStateException("basis has no positions");
			this.positional = positional;
			return this;
		}
		
		public Builder select(int index) {
			if (basis == null) throw new IllegalStateException("no basis");
			if (index < 0) throw new IllegalArgumentException("negative index");
			if (index >= basis.columns.size()) throw new IllegalArgumentException("invalid index");
			for (ColumnDefinition column : columns) {
				if (column.getBasis() == index) throw new IllegalArgumentException("duplicate basis index: " + index);
			}
			this.index = index;
			return this;
		}
		
		public Builder type(ColumnType type) {
			if (basis != null) throw new IllegalStateException("basis");
			if (type == null) throw new IllegalArgumentException("null type");
			this.type = type;
			return this;
		}
		
		public Builder order(ColumnOrder order) {
			this.order = order;
			return this;
		}
		
		public Builder add() {
			if (basis == null) {
				if (type == null) throw new IllegalStateException("no type");
				columns.add(new ColumnDefinition(columns.size(), type, order, -1));
			} else {
				if (index < 0) throw new IllegalStateException("no index");
				ColumnDefinition column = basis.basis == null ?
						basis.columns.get(index) :
						basis.basis.columns.get(basis.columns.get(index).getBasis());
				ColumnOrder colOrd = order == null ? column.getOrder() : order;
				columns.add(new ColumnDefinition(columns.size(), column.getType(), colOrd, column.getIndex()));
			}
			index = -1;
			type = null;
			order = null;
			return this;
		}
		
		public RecordDefinition build() {
			try {
				return new RecordDefinition(this);
			} finally {
				columns = new ArrayList<ColumnDefinition>();
			}
		}
		
		RecordDefinition getBasis() {
			if (basis == null) return null;
			if (basis.basis == null) return basis;
			return basis.basis;
		}
		
		Builder withIndices(int[] indices) {
			for (int index : indices) {
				select(index);
				add();
			}
			return this;
		}
		
		Builder withOrdering(List<ColumnOrder> orders) {
			for (int i = 0; i < basis.columns.size(); i++) {
				select(i);
				if (i < orders.size()) order(orders.get(i));
				add();
			}
			return this;
		}
		
		Builder withIndexedOrdering(List<ColumnOrder.Indexed> orders) {
			int count = basis.columns.size();
			List<ColumnOrder> list = new ArrayList<ColumnOrder>(count);
			list.addAll(Collections.nCopies(count, (ColumnOrder) null));
			for (ColumnOrder.Indexed indexed : orders) {
				if (indexed == null) continue;
				ColumnOrder order = list.set(indexed.getIndex(), indexed.getOrder());
				if (order != null) throw new IllegalArgumentException("duplicate order index: " + indexed.getIndex());
			}
			return withOrdering(list);
		}
		
	}

	public static Builder fromScratch() {
		return new Builder(null);
	}

	public static Builder fromTypes(List<ColumnType> types) {
		Builder builder = new Builder(null);
		for (ColumnType type : types) {
			builder.type(type).add();
		}
		return builder;
	}
	
	private static List<ColumnDefinition> asCompactOrder(List<ColumnDefinition> columns) {
		if (columns.isEmpty()) return columns;
		List<ColumnDefinition> orderedColumns = new ArrayList<ColumnDefinition>();
		for (ColumnDefinition column : columns) {
			if (column.getOrder() != null) orderedColumns.add(column);
		}
		if (orderedColumns.isEmpty()) return columns;
		Collections.sort(orderedColumns, ColumnOrder.columnComparator);

		List<ColumnDefinition> list = new ArrayList<ColumnDefinition>(columns);
		for (int precedence = 0; precedence < orderedColumns.size(); precedence++) {
			ColumnDefinition column = orderedColumns.get(precedence);
			ColumnOrder order = column.getOrder();
			if (order.getPrecedence() != precedence) {
				order = new ColumnOrder(precedence, order.isAscending(), order.isNullFirst());
				column = new ColumnDefinition(column.getIndex(), column.getType(), order, column.getBasis());
				list.set(column.getIndex(), column);
			}
		}
		return list;
	}
	
	private static List<ColumnDefinition> asOrderList(List<ColumnDefinition> columns) {
		int columnCount = columns.size();
		int orderCount = 0;
		for (int i = 0; i < columnCount; i++) {
			if (columns.get(i).getOrder() != null) orderCount ++;
		}
		List<ColumnDefinition> list = new ArrayList<ColumnDefinition>(orderCount);
		list.addAll(Collections.nCopies(orderCount, (ColumnDefinition) null));
		for (int i = 0; i < columnCount; i++) {
			ColumnDefinition column = columns.get(i);
			ColumnOrder order = column.getOrder();
			if (order != null) list.set(order.getPrecedence(), column);
		}
		return list;
	}
	
	private static List<ColumnType> asTypeList(List<ColumnDefinition> columns) {
		List<ColumnType> types = new ArrayList<ColumnType>();
		int count = columns.size();
		for (int i = 0; i < count; i++) {
			types.add(columns.get(i).getType());
		}
		return types;
	}
	
	static HashSource<RecordDefinition> hashSource = new HashSource<RecordDefinition>() {
		
		@Override
		public void sourceData(RecordDefinition definition, WriteStream out) {
			if (definition.basis != null) hashSource.sourceData(definition.basis, out); 
			out.writeBoolean(definition.ordinal);
			out.writeBoolean(definition.positional);
			for (ColumnDefinition column : definition.columns) {
				ColumnDefinition.hashSource.sourceData(column, out);
			}
		}
	};
	
	private static final int hashDigits = 10;
	
	private static HashRange hashRange = new HashRange(BigInteger.ZERO, BigInteger.ONE.shiftLeft(4 * hashDigits));
	
	private static PRNGMultiHash<RecordDefinition> hash = new PRNGMultiHash<RecordDefinition>(hashSource, hashRange);
	
	private static final String idPattern = "%0" + hashDigits + "X";

	// fields

	private final RecordDefinition basis;
	private final boolean ordinal;
	private final boolean positional;
	private final List<ColumnDefinition> columns;
	private final List<ColumnType> types;
	private final List<ColumnDefinition> orderedColumns;

	private String id = null;
	
	// constructors

	RecordDefinition(Builder builder) {
		basis = builder.getBasis();
		ordinal = builder.ordinal;
		positional = builder.positional;
		columns = Collections.unmodifiableList(asCompactOrder(builder.columns));
		types = Collections.unmodifiableList(asTypeList(columns));
		orderedColumns = Collections.unmodifiableList(asOrderList(columns));
	}
	
	RecordDefinition(RecordDefinition that) {
		basis = null;
		ordinal = that.ordinal;
		positional = that.positional;
		columns = Collections.unmodifiableList(asCompactOrder(that.columns));
		types = that.types;
		orderedColumns = Collections.unmodifiableList(asOrderList(columns));
	}
	
	// accessors
	
	public RecordDefinition getBasis() {
		return basis;
	}
	
	public RecordDefinition getBasisOrSelf() {
		return basis == null ? this : basis;
	}
	
	public boolean isOrdinal() {
		return ordinal;
	}
	
	public boolean isPositional() {
		return positional;
	}
	
	public List<ColumnDefinition> getColumns() {
		return columns;
	}
	
	public List<ColumnType> getTypes() {
		return types;
	}
	
	public List<ColumnDefinition> getOrderedColumns() {
		return orderedColumns;
	}
	
	public String getId() {
		if (id == null) {
//			String str = String.format(idPattern, hash.hashAsBigInt(this));
//			id = basis == null ? str : basis.getId() + '_' + str;
			id = String.format(idPattern, hash.hashAsBigInt(this));
		}
		return id;
	}
	
	public ColumnDefinition getBasisColumn(int index) {
		if (basis == null) throw new IllegalStateException("no basis");
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (index >= columns.size()) throw new IllegalArgumentException("invalid index");
		return basis.columns.get(columns.get(index).getBasis());
	}

	public List<ColumnDefinition> getBasisColumns() {
		if (basis == null) throw new IllegalStateException("null basis");
		List<ColumnDefinition> basisColumns = basis.columns;
		int count = basisColumns.size();
		
		List<ColumnDefinition> list = new ArrayList<ColumnDefinition>(count);
		list.addAll(Collections.nCopies(count, (ColumnDefinition) null));
		for (ColumnDefinition column : columns) {
			list.set(column.getBasis(), column);
		}
		return list;
	}
	
	public <E> void adaptBasicList(List<E> list) {
		if (basis == null) throw new IllegalStateException("no basis");
		if (list == null) return;
		int basisSize = basis.columns.size();
		int size = list.size();
		if (size < basisSize) {
			list.addAll((List)Collections.nCopies(basisSize - size, null));
		} else if (size > basisSize) {
			list.subList(basisSize, size).clear();
		}
		for (ColumnDefinition column : columns) {
			list.add(list.get(column.getBasis()));
		}
		list.subList(0, basisSize).clear();
	}
	
	// methods
	
	public RecordDefinition asBasis() {
		return basis == null ? this : new RecordDefinition(this);
	}

	public Builder asBasisToBuild() {
		return new Builder(this);
	}
	
	public Builder asCompleteBasisToBuild() {
		Builder builder = new Builder(this);
		int count = columns.size();
		for (int i = 0; i < count; i++) {
			builder.select(i).add();
		}
		return builder;
	}
	
	public RecordDefinition withIndices(int[] indices) {
		if (indices == null || indices.length == 0) return this;
		return new Builder(this).withIndices(indices).build();
	}
	
	public RecordDefinition withOrdering(List<ColumnOrder> orders) {
		//TODO could check if orders are exclusively null
		if (orders == null || orders.isEmpty()) return this;
		return new Builder(this).withOrdering(orders).build();
	}
	
	public RecordDefinition withIndexedOrdering(List<ColumnOrder.Indexed> orders) {
		if (orders == null || orders.isEmpty()) return this;
		return new Builder(this).withIndexedOrdering(orders).build();
	}
	
	public RecordDefinition asSubRecord(SubRecordDefinition subRecDef) {
		if (subRecDef == null) throw new IllegalArgumentException("null subRecDef");
		RecordDefinition basis;
		if (ordinal && !subRecDef.isOrdinalRetained() || positional && !subRecDef.isPositionRetained()) {
			basis = asCompleteBasisToBuild()
				.setOrdinal(ordinal && subRecDef.isOrdinalRetained())
				.setPositional(positional && subRecDef.isPositionRetained())
				.build();
		} else {
			basis = this;
		}
		int[] indices = subRecDef.getIndicesUnsafely();
		if (indices != null) basis = basis.withIndices(indices);
		List<Indexed> orders = subRecDef.getOrders();
		if (orders != null) basis = basis.withIndexedOrdering(orders);
		return basis;
	}

	//TODO object methods
	
	@Override
	public String toString() {
		return "ordinal: " + ordinal + ", positional: " + positional + ", columns: " + columns;
	}
	
}