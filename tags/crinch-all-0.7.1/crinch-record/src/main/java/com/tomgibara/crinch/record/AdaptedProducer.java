/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record;

import java.util.NoSuchElementException;

import com.tomgibara.crinch.record.process.ProcessContext;

public abstract class AdaptedProducer<R extends Record, S extends Record> implements RecordProducer<S> {

	protected final RecordProducer<R> producer;
	
	public AdaptedProducer(RecordProducer<R> producer) {
		if (producer == null) throw new IllegalArgumentException("null producer");
		this.producer = producer;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		producer.prepare(context);
	}
	
	@Override
	public RecordSequence<S> open() {
		return new AdaptedSequence<R, S>(producer.open()) {
			
			private S next;
			
			{
				advance();
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
			
			@Override
			public S next() {
				if (next == null) throw new NoSuchElementException();
				try {
					return next;
				} finally {
					advance();
				}
			}
			
			private void advance() {
				while (sequence.hasNext()) {
					next = adapt(sequence.next());
					if (next != null) return;
				}
				next = null;
			}
			
		};
	}
	
	@Override
	public void complete() {
		producer.complete();
	}
	
	// records may be filtered-out by returning null
	protected abstract S adapt(R record);
	
}
