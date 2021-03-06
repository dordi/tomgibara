/*
 * Copyright 2009 Tom Gibara
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
package com.tomgibara.stupp;

public class StuppSequence<K> {

	public interface Generator<K> {
		
		K next();
		
	}
	
	private final Generator<K> generator;
	private final StuppLock lock;
	
	public StuppSequence(Generator<K> generator) {
		this(generator, null);
	}
	
	public StuppSequence(Generator<K> generator, StuppLock lock) {
		if (generator == null) throw new IllegalArgumentException();
		this.generator = generator;
		this.lock = lock == null ? StuppLock.NOOP_LOCK : lock;
	}

	public StuppLock getLock() {
		return lock == StuppLock.NOOP_LOCK ? null : lock;
	}

	public K next() {
		lock.lock();
		try {
			return generator.next();
		} finally {
			lock.unlock();
		}
	}
	
}
