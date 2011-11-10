package com.tomgibara.crinch.record;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.util.WriteStream;

public class RecordDefinition {
	
	// statics
	
	private static HashSource<RecordDefinition> hashSource = new HashSource<RecordDefinition>() {
		
		@Override
		public void sourceData(RecordDefinition definition, WriteStream out) {
			// whether or not records contain them is not really part of name
//			out.writeBoolean(definition.ordinal);
//			out.writeBoolean(definition.positional);
			for (ColumnType type : definition.types) {
				out.writeInt(type.ordinal());
			}
			for (ColumnOrder order: definition.orders) {
				out.writeInt(order.getIndex());
				out.writeBoolean(order.isAscending());
				out.writeBoolean(order.isNullFirst());
			}
		}
	};
	
	private static final int hashDigits = 10;
	
	private static HashRange hashRange = new HashRange(BigInteger.ZERO, BigInteger.ONE.shiftLeft(4 * hashDigits));
	
	private static PRNGMultiHash<RecordDefinition> hash = new PRNGMultiHash<RecordDefinition>(hashSource, hashRange);
	
	private static final String idPattern = "%0" + hashDigits + "X";

	// fields
	
	//TODO consider moving off definition and onto factory constructor
	private final boolean ordinal;
	private final boolean positional;
	private final List<ColumnType> types;
	private final List<ColumnOrder> orders;

	private String id = null;
	
	// constructors
	
	public RecordDefinition(boolean ordinal, boolean positional, List<ColumnType> types, ColumnOrder... orders) {
		if (types == null) throw new IllegalArgumentException("null types");
		if (orders == null) throw new IllegalArgumentException("null orders");
		
		this.ordinal = ordinal;
		this.positional = positional;
		this.types = Collections.unmodifiableList(new ArrayList<ColumnType>(types));
		this.orders = Collections.unmodifiableList(new ArrayList<ColumnOrder>(Arrays.asList(orders)));

		// verify input
		if (this.types.contains(null)) throw new IllegalArgumentException("null type");
		if (this.orders.contains(null)) throw new IllegalArgumentException("null order");
		
		final int size = this.types.size();
		BitVector vector = new BitVector(size);
		for (ColumnOrder order : this.orders) {
			int index = order.getIndex();
			if (index < 0 || index >= size) throw new IllegalArgumentException("invalid order index: " + index);
			if (vector.getBit(index)) throw new IllegalArgumentException("duplicate order index: " + index);
			vector.setBit(index, true);
		}
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
	
	public List<ColumnOrder> getOrders() {
		return orders;
	}
	
	public String getId() {
		if (id == null) {
			id = String.format(idPattern, hash.hashAsBigInt(this));
		}
		return id;
	}
	
}