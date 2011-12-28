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
package com.tomgibara.crinch.record.process;

import com.tomgibara.crinch.record.Record;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordTransfer;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;

public class RecordProcessor {

	private ProcessContext context = null;
	
	public RecordProcessor() {
	}

	public RecordProcessor(ProcessContext context) {
		this.context = context;
	}
	
	public void setContext(ProcessContext context) {
		this.context = context;
	}
	
	public ProcessContext getContext() {
		return context;
	}
	
	/**
	 * Prepares the producer and consumer and transfers all records from one to
	 * the other until processing is complete. Exceptions that occur during
	 * processing are caught and recorded in the context's logger.
	 * 
	 * @param producer
	 *            the record producer
	 * @param consumer
	 *            the record consumer
	 * @return true iff the process ran to completion without error
	 */
	
	public <R extends Record> boolean process(RecordProducer<R> producer, RecordConsumer<R> consumer) {
		if (context == null) throw new IllegalStateException("null context");
		int state = 0;
		try {
			producer.prepare(context);
			state = 1;
			consumer.prepare(context);
			state = 2;
			new RecordTransfer<R>(producer, consumer).transfer(context);
			state = 3;
			consumer.complete();
			state = 4;
			producer.complete();
			state = 5;
		} catch (RuntimeException e) {
			context.getLogger().log("error processing records", e);
		} finally {
			if (state == 4) {
				state = 0;
			}
			if (state == 3) {
				state = 1;
			}
			if (state == 2) {
				try {
					consumer.quit();
				} catch (RuntimeException ex) {
					context.getLogger().log(Level.WARN, "error terminating consumption", ex);
				} finally {
					state = 1;
				}
			}
			if (state == 1) {
				try {
					producer.complete();
				} catch (RuntimeException ex) {
					context.getLogger().log(Level.WARN, "error terminating production", ex);
				} finally {
					state = 0;
				}
			}
		}
		return state == 5;
	}
	
}
