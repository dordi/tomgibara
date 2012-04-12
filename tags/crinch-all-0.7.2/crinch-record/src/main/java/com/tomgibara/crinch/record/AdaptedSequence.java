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

public abstract class AdaptedSequence<R, S> implements RecordSequence<S> {

	protected RecordSequence<R> sequence;
	
	public AdaptedSequence(RecordSequence<R> sequence) {
		if (sequence == null) throw new IllegalArgumentException("null sequence");
		this.sequence = sequence;
	}
	
	@Override
	public void remove() {
		sequence.remove();
	}
	
	@Override
	public boolean hasNext() {
		return sequence.hasNext();
	}
	
	@Override
	public void close() {
		sequence.close();
	}

	
	
}
