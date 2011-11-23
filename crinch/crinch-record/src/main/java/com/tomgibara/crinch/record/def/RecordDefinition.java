package com.tomgibara.crinch.record.def;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
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
			if (order != null) {
				int precedence = order.getPrecedence();
				for (ColumnDefinition column : columns) {
					ColumnOrder colOrd = column.getOrder();
					if (colOrd != null && colOrd.getPrecedence() == precedence) {
						throw new IllegalArgumentException("duplicate order precedence");
					}
				}
			}
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
	
	/*
	//TODO this contains a gotcha for clients constructing definitions
	private static List<ColumnDefinition> asColumnList(Collection<ColumnDefinition> columns) {
		if (columns == null) return null;
		int count = columns.size();
		List<ColumnDefinition> list;
		if (columns instanceof List) {
			list = new ArrayList<ColumnDefinition>(columns);
			for (int i = 0; i < count; i++) {
				final ColumnDefinition column = list.get(i);
				if (column == null) throw new IllegalArgumentException("null column");
				if (column.getIndex() != i) throw new IllegalArgumentException("column index incorrect at " + i);
			}
		} else {
			list = new ArrayList<ColumnDefinition>(count);
			list.addAll((Collection) Collections.nCopies(count, null));
			for (ColumnDefinition column : columns) {
				if (column == null) throw new IllegalArgumentException("null column");
				int index = column.getIndex();
				if (index >= count) throw new IllegalArgumentException("invalid column index: " + index);
				if (list.set(index, column) != null) throw new IllegalArgumentException("duplicate column index: " + index); 
			}
		}
		return list;
	}
	
	private static List<ColumnDefinition> asColumnList(List<ColumnType> types, List<ColumnOrder> orders) {
		if (types == null) return null;
		int count = types.size();
		if (orders != null && orders.size() > count) throw new IllegalArgumentException("too many orders");
		List<ColumnDefinition> definitions = new ArrayList<ColumnDefinition>();
		for (int i = 0; i < count; i++) {
			definitions.add(i, new ColumnDefinition(i, types.get(i), orders == null || i >= orders.size() ? null : orders.get(i)));
		}
		return definitions;
	}
	*/
	
	private static List<ColumnDefinition> asOrderList(List<ColumnDefinition> columns) {
		int count = columns.size();
		int limit = 0;
		for (int i = 0; i < count; i++) {
			if (columns.get(i).getOrder() != null) limit ++;
		}
		List<ColumnDefinition> list = new ArrayList<ColumnDefinition>(limit);
		for (int precedence = 0; precedence < limit; precedence++) {
			ColumnDefinition next = null;
			for (int i = 0; i < count; i++) {
				ColumnDefinition column = columns.get(i);
				ColumnOrder order = column.getOrder();
				if (order == null) continue;
				if (order.getPrecedence() == precedence) {
					if (next != null) throw new IllegalArgumentException("duplicate column order precedence: " + precedence);
					next = column;
				}
			}
			if (next == null) throw new IllegalArgumentException("precedence absent: " + precedence);
			list.add(next);
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
		columns = Collections.unmodifiableList(builder.columns);
		types = Collections.unmodifiableList(asTypeList(columns));
		orderedColumns = Collections.unmodifiableList(asOrderList(columns));
	}
	
	RecordDefinition(RecordDefinition that) {
		basis = null;
		ordinal = that.ordinal;
		positional = that.positional;
		columns = that.columns;
		types = that.types;
		orderedColumns = that.orderedColumns;
	}
	
	// accessors
	
	public RecordDefinition getBasis() {
		return basis;
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
			String str = String.format(idPattern, hash.hashAsBigInt(this));
			id = basis == null ? str : basis.getId() + '_' + str;
		}
		return id;
	}
	
	public ColumnDefinition getBasisColumn(int index) {
		if (basis == null) throw new IllegalStateException("null basis");
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
	
	public RecordDefinition withOrdering(List<ColumnOrder> orders) {
		//TODO could check if orders are exclusively null
		if (orders == null || orders.isEmpty()) return this;
		Builder builder = new Builder(this);
		for (int i = 0; i < columns.size(); i++) {
			builder.select(i);
			if (i < orders.size()) builder.order(orders.get(i));
			builder.add();
		}
		return builder.build();
	}
	
}