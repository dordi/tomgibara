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

public interface RecordConsumer<R extends Record> {

	void prepare(ProcessContext context);

	int getRequiredPasses();
	
	void beginPass();
	
	/**
	 * Supplies a record to the consumer during processing.
	 * During processing, the record supplied to this method will automatically be released at some time after the the call to consume has returned.
	 * Though implementations are free to release records sooner by directly calling {@link Record#release()} it is not necessary since the record will be released on the consumer's behalf.
	 * However, this has the consequence that consumers cannot safely keep references to a record (or any resource it may back) beyond the call.
	 * 
	 * @param record a record, never null
	 */
	
	void consume(R record);
	
	void endPass();
	
	void complete();

	// stop immediately without further processing
	void quit();
	
}
