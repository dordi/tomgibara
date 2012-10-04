package com.tomgibara.crinch.record.fact;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.tomgibara.crinch.record.def.RecordDef;

public final class FactDomain {

	// fields
	
	private final RecordDef recordDef;
	private final Map<String, AssertionType<?>> types;
	
	// constructors
	
	public FactDomain(RecordDef recordDef, Collection<AssertionType<?>> types) {
		if (recordDef == null) throw new IllegalArgumentException("null recordDef");
		if (!recordDef.isBasic()) throw new IllegalArgumentException("recordDef not basic");
		if (types == null) throw new IllegalArgumentException("null types");
		HashMap<String, AssertionType<?>> map = new HashMap<String, AssertionType<?>>(types.size());
		for (AssertionType<?> type : types) {
			if (type == null) throw new IllegalArgumentException("null type");
			boolean wasNew = map.put(type.getAssertionClass().getName(), type) == null;
			if (!wasNew) throw new IllegalArgumentException("duplicate type name");
		}
		this.recordDef = recordDef;
		this.types = Collections.unmodifiableMap(map);
	}
	
	// accessors
	
	public RecordDef getRecordDef() {
		return recordDef;
	}
	
	public Collection<AssertionType<?>> getTypes() {
		return types.values();
	}

	public <A> AssertionType<A> getType(Class<A> assertionClass) {
		AssertionType<A> type = (AssertionType<A>) types.get(assertionClass.getName());
		if (type == null) return type;
		return type == null || type.getClass() != assertionClass ? null : type;
	}

	<A> AssertionType<A> getType(String assertionClassName) {
		return (AssertionType<A>) types.get(assertionClassName);
	}

	// object methods
	
	@Override
	public int hashCode() {
		return recordDef.hashCode() ^ types.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof FactDomain)) return false;
		FactDomain that = (FactDomain) obj;
		if (!this.recordDef.equals(that.recordDef)) return false;
		if (!this.types.equals(that.types)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "record-def: " + recordDef + " types: " + types;
	}
	
}
