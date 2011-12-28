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

import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;


public class RecordTransfer<R extends Record> {

	private final RecordProducer<R> producer;
	private final RecordConsumer<R> consumer;
	
	private long recordNumber = 0L;
	
	public RecordTransfer(RecordProducer<R> producer, RecordConsumer<R> consumer) {
		if (producer == null) throw new IllegalArgumentException("null producer");
		if (consumer == null) throw new IllegalArgumentException("null consumer");
		this.producer = producer;
		this.consumer = consumer;
	}
	
	public void transfer(ProcessContext context) {
		while (consumer.getRequiredPasses() != 0) {
			RecordSequence<R> sequence = producer.open();
			if (sequence == null) throw new RuntimeException("null record sequence from producer");
			try {
				consumer.beginPass();
				context.setRecordsTransferred(recordNumber = 0);
				while (sequence.hasNext()) {
					R record = sequence.next();
					try {
						consumer.consume(record);
					} finally {
						record.release();
					}
					context.setRecordsTransferred(++recordNumber);
				}
				consumer.endPass();
			} finally {
				try {
					sequence.close();
				} catch (RuntimeException e) {
					context.getLogger().log(Level.WARN, "error closing sequence", e);
				}
			}
		}
	}
}
