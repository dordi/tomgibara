package com.tomgibara.crinch.record;

import java.util.Iterator;

public interface RecordSequence<R> extends Iterator<R> {

	void close();
	
}
