package com.tomgibara.crinch.record.fact;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.def.ColumnDef;


public class Facts {

	public static Facts read(FactDomain domain, CodedReader reader) {
		Facts facts = new Facts(domain);
		int typeCount = reader.readInt();
		for (int i = 0; i < typeCount; i++) {
			String typeClass = CodedStreams.readString(reader);
			AssertionType<Object> type = domain.getType(typeClass);
			//TODO better exception needed?
			if (type == null) throw new IllegalArgumentException("No assertion type for class: " + typeClass);
			int factCount = reader.readInt();
			for (int j = 0; j < factCount; j++) {
				Object assertion = type.read(reader);
				int[] columnIndices = CodedStreams.readIntArray(reader);
				Fact<Object> fact = new Fact<Object>(domain, type, assertion, columnIndices);
				facts.add(fact);
			}
		}
		return facts;
	}
	
	private final FactDomain domain;
	
	private final Set<Fact<?>> facts = new HashSet<Fact<?>>();
	private final Map<AssertionType<?>, Set<Fact<?>>> factsByType = new HashMap<AssertionType<?>, Set<Fact<?>>>();
	//TODO provide more indexed access to facts
	
	
	public Facts(FactDomain domain) {
		if (domain == null) throw new IllegalArgumentException("null domain");
		this.domain = domain;
	}
	
	public FactDomain getDomain() {
		return domain;
	}
	
	public void add(Fact<?> fact) {
		if (fact == null) throw new IllegalArgumentException("null fact");
		//TODO prevent contradictory facts
		facts.add(fact);
		{
			AssertionType<?> type = fact.getType();
			Set<Fact<?>> set = factsByType.get(type);
			if (set == null) {
				set = new HashSet<Fact<?>>();
				factsByType.put(fact.getType(), set);
			}
			set.add(fact);
		}
	}

	//TODO address minor risk posed by multiple classloaders
	public void write(CodedWriter writer) {
		writer.writeInt(factsByType.size());
		for (Entry<AssertionType<?>, Set<Fact<?>>> entry : factsByType.entrySet()) {
			AssertionType<?> type = entry.getKey();
			CodedStreams.writeString(writer, type.getAssertionClass().getName());
			Set<Fact<?>> set = entry.getValue();
			writer.writeInt(set.size());
			for (Fact<?> fact : set) {
				Collection<ColumnDef> columns = fact.getColumns();
				int[] columnIndices = new int[columns.size()];
				int count = 0;
				for (ColumnDef column : columns) {
					columnIndices[count++] = column.getIndex();
				}
				type.write(writer, fact.getAssertion());
				CodedStreams.writePrimitiveArray(writer, columnIndices);
			}
		}
	}

}