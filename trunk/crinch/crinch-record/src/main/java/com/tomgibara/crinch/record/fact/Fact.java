package com.tomgibara.crinch.record.fact;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.tomgibara.crinch.record.def.ColumnDef;

public final class Fact<A> {

	// fields
	
	private final FactDomain domain;
	private final AssertionType<A> type;
	private final A assertion;
	private final Collection<ColumnDef> columns;
	
	// constructors
	
	public Fact(FactDomain domain, AssertionType<A> type, A assertion, int... columnIndices) {
		if (domain == null) throw new IllegalArgumentException("null domain");
		if (type == null) throw new IllegalArgumentException("null type");
		if (assertion == null) throw new IllegalArgumentException("null assertion");
		Collection<ColumnDef> columns = domain.getRecordDef().getColumns(columnIndices);
		if (!type.isColumnOrderDependent()) columns = new LinkedHashSet<ColumnDef>(columns);
		this.domain = domain;
		this.type = type;
		this.assertion = assertion;
		this.columns = columns;
	}
	
	// accessors
	
	public FactDomain getDomain() {
		return domain;
	}
	
	public AssertionType<A> getType() {
		return type;
	}
	
	public A getAssertion() {
		return assertion;
	}
	
	//a list if order dependent, a set otherwise
	public Collection<ColumnDef> getColumns() {
		return columns;
	}
	
	// object methods
	
	@Override
	public int hashCode() {
		return domain.hashCode() ^ assertion.hashCode() ^ columns.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Fact)) return false;
		Fact<?> that = (Fact<?>) obj;
		if (!this.domain.equals(that.domain)) return false;
		if (!this.assertion.equals(that.assertion)) return false;
		if (!columns.equals(that.columns)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return assertion.toString() + " " + columns;
	}
	
}
