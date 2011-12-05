/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.WriteStream;

public class LongSeededHashSource<T> extends SeededHashSource<T> {

	private final long seed;
	
	public LongSeededHashSource(HashSource<T> source, long seed) {
		super(source);
		this.seed = seed;
	}

	@Override
	protected void seed(WriteStream out) {
		out.writeLong(seed);
	}
	
}
