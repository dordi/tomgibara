package com.tomgibara.crinch.record.fact;

import java.util.Collection;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;

public interface Asserter<A> extends RecordConsumer<LinearRecord> {

	AssertionType<A> getAssertionType();
	
	// called before prepare
	void setColumnIndices(int... columnIndices);
	
	// called after complete() and before prepare()
	Collection<A> getAssertions();
	
}
