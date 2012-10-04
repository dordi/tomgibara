package com.tomgibara.crinch.record.fact;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedWriter;

public interface AssertionType<A> {

	Class<A> getAssertionClass();

	boolean isColumnOrderDependent();
	
	void write(CodedWriter writer, Object assertion);
	
	A read(CodedReader reader);
	
}
