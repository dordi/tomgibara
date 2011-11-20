package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.record.EmptyRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;

public class PositionProducer implements RecordProducer<EmptyRecord> {

	private PositionStats stats;
	
	@Override
	public void prepare(ProcessContext context) {
		stats = new PositionStats(context);
		stats.read();
	}
	
	@Override
	public RecordSequence<EmptyRecord> open() {
		return new PositionSequence();
	}

	@Override
	public void complete() {
	}

	private class PositionSequence implements RecordSequence<EmptyRecord> {

		PositionSequence() {
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public EmptyRecord next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
