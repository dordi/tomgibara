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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.record.def.ColumnOrder.Sort;
import com.tomgibara.crinch.util.WriteStream;

public class RecordDef {
	
	// statics
	
	// slightly hacky way of recording that builder clients are specifically nullifying an order
	private static final ColumnOrder NO_ORDER = new ColumnOrder(0, Sort.ASCENDING, true);
	private static final Map<String, String> NO_PROPS = Collections.emptyMap();

	private static Map<String, String> combineProperties(Map<String, String> basisProps, Map<String, String> extraProps) {
		if (extraProps.isEmpty()) return basisProps;
		Map<String, String> props = new LinkedHashMap<String, String>(basisProps);
		for (Entry<String, String> entry : extraProps.entrySet()) {
			if (entry.getValue() == null) {
				props.remove(entry.getKey());
			} else {
				props.put(entry.getKey(), entry.getValue());
			}
		}
		return Collections.unmodifiableMap(props);
	}
	
	public static class Builder {
		
		private final RecordDef basis;

		private boolean ordinal;
		private boolean positional;
		private final Map<String, String> recordProps = new LinkedHashMap<String, String>();
		private List<ColumnDef> columns = new ArrayList<ColumnDef>();
		private int index = -1;
		private ColumnType type;
		private ColumnOrder order;
		private final Map<String, String> columnProps = new LinkedHashMap<String, String>();
		
		Builder(RecordDef basis) {
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
		
		public Builder recordProp(String name, String value) {
			if (name == null) throw new IllegalArgumentException("null name");
			recordProps.put(name, value);
			return this;
		}
		
		public Builder select(int index) {
			if (basis == null) throw new IllegalStateException("no basis");
			if (index < 0) throw new IllegalArgumentException("negative index");
			if (index >= basis.columns.size()) throw new IllegalArgumentException("invalid index");
			for (ColumnDef column : columns) {
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
			this.order = order == null ? NO_ORDER : order;
			return this;
		}
		
		public Builder columnProp(String name, String value) {
			if (name == null) throw new IllegalArgumentException("null name");
			columnProps.put(name, value);
			return this;
		}
		
		public Builder add() {
			if (basis == null) {
				if (type == null) throw new IllegalStateException("no type");
				columns.add(new ColumnDef(columns.size(), type, order, -1, combineProperties(NO_PROPS, columnProps)));
			} else {
				if (index < 0) throw new IllegalStateException("no index");
				ColumnDef column = basis.columns.get(index);
				ColumnOrder colOrd = order == null ? column.getOrder() : (order == NO_ORDER ? null : order);
				int index = basis.basis == null ? this.index : column.getBasis(); 
				Map<String, String> properties = combineProperties(column.getProperties(), this.columnProps);
				columns.add(new ColumnDef(columns.size(), column.getType(), colOrd, index, properties));
			}
			index = -1;
			type = null;
			order = null;
			columnProps.clear();
			return this;
		}

		public RecordDef build() {
			if (index != -1 || type != null || order != null || !columnProps.isEmpty()) throw new IllegalStateException("unadded column information");
			try {
				return new RecordDef(this);
			} finally {
				columns = new ArrayList<ColumnDef>();
				recordProps.clear();
			}
		}
		
		RecordDef getBasis() {
			if (basis == null) return null;
			if (basis.basis == null) return basis;
			return basis.basis;
		}
		
		Map<String, String> getProperties() {
			return combineProperties(basis == null ? NO_PROPS : basis.properties, recordProps);
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
				order( i < orders.size() ? orders.get(i) : null);
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
	
	private static List<ColumnDef> asCompactOrder(List<ColumnDef> columns) {
		if (columns.isEmpty()) return columns;
		List<ColumnDef> orderedColumns = new ArrayList<ColumnDef>();
		for (ColumnDef column : columns) {
			if (column.getOrder() != null) orderedColumns.add(column);
		}
		if (orderedColumns.isEmpty()) return columns;
		Collections.sort(orderedColumns, ColumnOrder.columnComparator);

		List<ColumnDef> list = new ArrayList<ColumnDef>(columns);
		for (int precedence = 0; precedence < orderedColumns.size(); precedence++) {
			ColumnDef column = orderedColumns.get(precedence);
			ColumnOrder order = column.getOrder();
			if (order.getPrecedence() != precedence) {
				list.set(column.getIndex(), column.withOrder(order.withPrecedence(precedence)));
			}
		}
		return list;
	}
	
	private static List<ColumnDef> asOrderList(List<ColumnDef> columns) {
		int columnCount = columns.size();
		int orderCount = 0;
		for (int i = 0; i < columnCount; i++) {
			if (columns.get(i).getOrder() != null) orderCount ++;
		}
		List<ColumnDef> list = new ArrayList<ColumnDef>(orderCount);
		list.addAll(Collections.nCopies(orderCount, (ColumnDef) null));
		for (int i = 0; i < columnCount; i++) {
			ColumnDef column = columns.get(i);
			ColumnOrder order = column.getOrder();
			if (order != null) list.set(order.getPrecedence(), column);
		}
		return list;
	}
	
	private static List<ColumnType> asTypeList(List<ColumnDef> columns) {
		List<ColumnType> types = new ArrayList<ColumnType>();
		int count = columns.size();
		for (int i = 0; i < count; i++) {
			types.add(columns.get(i).getType());
		}
		return types;
	}
	
	static HashSource<RecordDef> hashSource = new HashSource<RecordDef>() {
		
		@Override
		public void sourceData(RecordDef definition, WriteStream out) {
			if (definition.basis != null) hashSource.sourceData(definition.basis, out); 
			out.writeBoolean(definition.ordinal);
			out.writeBoolean(definition.positional);
			for (ColumnDef column : definition.columns) {
				ColumnDef.hashSource.sourceData(column, out);
			}
		}
	};
	
	private static final int hashDigits = 10;
	
	private static HashRange hashRange = new HashRange(BigInteger.ZERO, BigInteger.ONE.shiftLeft(4 * hashDigits));
	
	private static PRNGMultiHash<RecordDef> hash = new PRNGMultiHash<RecordDef>(hashSource, hashRange);
	
	private static final String idPattern = "%0" + hashDigits + "X";

	// fields

	private final RecordDef basis;
	private final boolean ordinal;
	private final boolean positional;
	private final List<ColumnDef> columns;
	private final List<ColumnType> types;
	private final List<ColumnDef> orderedColumns;
	private final Map<String, String> properties;

	private String id = null;
	
	// constructors

	RecordDef(Builder builder) {
		basis = builder.getBasis();
		ordinal = builder.ordinal;
		positional = builder.positional;
		columns = Collections.unmodifiableList(asCompactOrder(builder.columns));
		types = Collections.unmodifiableList(asTypeList(columns));
		orderedColumns = Collections.unmodifiableList(asOrderList(columns));
		properties = builder.getProperties();
	}
	
	RecordDef(RecordDef that) {
		basis = null;
		ordinal = that.ordinal;
		positional = that.positional;
		columns = Collections.unmodifiableList(asCompactOrder(that.columns));
		types = that.types;
		orderedColumns = Collections.unmodifiableList(asOrderList(columns));
		properties = that.properties;
	}
	
	// accessors
	
	public RecordDef getBasis() {
		return basis;
	}
	
	public RecordDef getBasisOrSelf() {
		return basis == null ? this : basis;
	}
	
	public boolean isOrdinal() {
		return ordinal;
	}
	
	public boolean isPositional() {
		return positional;
	}
	
	public List<ColumnDef> getColumns() {
		return columns;
	}
	
	public List<ColumnType> getTypes() {
		return types;
	}
	
	public List<ColumnDef> getOrderedColumns() {
		return orderedColumns;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public Boolean getBooleanProperty(String name) {
		String value = properties.get(name);
		if (value == null) return null;
		if (value.equalsIgnoreCase("true")) return Boolean.TRUE;
		if (value.equalsIgnoreCase("false")) return Boolean.FALSE;
		throw new IllegalArgumentException("property " + name + " not a boolean");
	}
	
	public Long getLongProperty(String name) {
		String value = properties.get(name);
		if (value == null) return null;
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("property " + name + " not a long", e);
		}
	}
	
	public String getId() {
		if (id == null) {
			id = String.format(idPattern, hash.hashAsBigInt(this));
		}
		return id;
	}
	
	public ColumnDef getBasisColumn(int index) {
		if (basis == null) throw new IllegalStateException("no basis");
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (index >= columns.size()) throw new IllegalArgumentException("invalid index");
		return basis.columns.get(columns.get(index).getBasis());
	}

	public List<ColumnDef> getBasisColumns() {
		if (basis == null) throw new IllegalStateException("null basis");
		List<ColumnDef> basisColumns = basis.columns;
		int count = basisColumns.size();
		
		List<ColumnDef> list = new ArrayList<ColumnDef>(count);
		list.addAll(Collections.nCopies(count, (ColumnDef) null));
		for (ColumnDef column : columns) {
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
		for (ColumnDef column : columns) {
			list.add(list.get(column.getBasis()));
		}
		list.subList(0, basisSize).clear();
	}
	
	// methods
	
	public RecordDef asBasis() {
		return basis == null ? this : new RecordDef(this);
	}

	public Builder asBasisToBuild() {
		return new Builder(this);
	}
	
	public RecordDef withRetention(boolean ordinalRetained, boolean positionRetained) {
		if ((!ordinal || ordinalRetained) && (!positional || positionRetained)) return this;
		return asCompleteBasisToBuild()
			.setOrdinal(ordinal && ordinalRetained)
			.setPositional(positional && positionRetained)
			.build();
	}
	
	public RecordDef withIndices(int[] indices) {
		if (indices == null) return this;
		return new Builder(this).withIndices(indices).build();
	}
	
	public RecordDef withOrdering(List<ColumnOrder> orders) {
		if (orders == null) return this;
		return new Builder(this).withOrdering(orders).build();
	}
	
	public RecordDef withIndexedOrdering(List<ColumnOrder.Indexed> orders) {
		if (orders == null) return this;
		return new Builder(this).withIndexedOrdering(orders).build();
	}
	
	public RecordDef withProperties(Map<String, String> properties) {
		if (properties == null || properties.isEmpty()) return this;
		Set<Entry<String,String>> entrySet = properties.entrySet();
		Builder builder = asCompleteBasisToBuild();
		for (Entry<String, String> property : entrySet) {
			builder.recordProp(property.getKey(), property.getValue());
		}
		return builder.build();
	}
	
	public RecordDef asSubRecord(SubRecordDef subRecDef) {
		if (subRecDef == null) throw new IllegalArgumentException("null subRecDef");
		return this
			.withRetention(subRecDef.isOrdinalRetained(), subRecDef.isPositionRetained())
			.withIndices(subRecDef.getIndicesUnsafely())
			.withIndexedOrdering(subRecDef.getOrders())
			.withProperties(subRecDef.getProperties());
	}

	private Builder asCompleteBasisToBuild() {
		Builder builder = new Builder(this);
		int count = columns.size();
		for (int i = 0; i < count; i++) {
			builder.select(i).add();
		}
		return builder;
	}
	
	//TODO object methods
	
	@Override
	public String toString() {
		return "ordinal: " + ordinal + ", positional: " + positional + ", columns: " + columns;
	}
	
}