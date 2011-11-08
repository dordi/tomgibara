package com.tomgibara.crinch.record.sort;

import com.tomgibara.crinch.record.ColumnOrder;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;

public class TrieConsumer extends OrderedConsumer {

	public TrieConsumer(ColumnOrder... orders) {
		super(orders);
	}
	
	@Override
	public void prepare(ProcessContext context) {
		super.prepare(context);
		if (!sortedFile().exists()) throw new IllegalStateException("no sorted file: " + sortedFile());
	}

	@Override
	public int getRequiredPasses() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void beginPass() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void consume(LinearRecord record) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endPass() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
