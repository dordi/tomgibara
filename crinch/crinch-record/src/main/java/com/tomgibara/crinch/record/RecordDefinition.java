package com.tomgibara.crinch.record;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.util.WriteStream;

public class RecordDefinition {
	
	// statics
	
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
	private final int[] basicIndices;

	private String id = null;
	
	// constructors

	public RecordDefinition(boolean ordinal, boolean positional, Collection<ColumnDefinition> columns) {
		if (columns == null) throw new IllegalArgumentException("null columns");
		basis = null;
		basicIndices = null;
		this.ordinal = ordinal;
		this.positional = positional;
		this.columns = Collections.unmodifiableList(asColumnList(columns));
		this.types = Collections.unmodifiableList(asTypeList(this.columns));
		this.orderedColumns = Collections.unmodifiableList(asOrderList(this.columns));
	}

	public RecordDefinition(boolean ordinal, boolean positional, List<ColumnType> types, List<ColumnOrder> orders) {
		this(ordinal, positional, asColumnList(types, orders));
	}

	// accessors
	
	public boolean isOrdinal() {
		return ordinal;
	}
	
	public boolean isPositional() {
		return positional;
	}
	
	public List<ColumnType> getTypes() {
		return types;
	}
	
	public List<ColumnDefinition> getOrderedColumns() {
		return orderedColumns;
	}
	
	public String getId() {
		if (id == null) {
			id = String.format(idPattern, hash.hashAsBigInt(this));
		}
		return id;
	}
	
}