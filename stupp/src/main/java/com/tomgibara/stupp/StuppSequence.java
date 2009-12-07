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

public abstract class StuppSequence<K> {

	private StuppLock lock;
	
	public StuppSequence() {
		this(null);
	}
	
	public StuppSequence(StuppLock lock) {
		this.lock = lock == null ? StuppLock.NOOP_LOCK : lock;
	}

	public StuppLock getLock() {
		return lock == StuppLock.NOOP_LOCK ? null : lock;
	}

	public K next() {
		lock.lock();
		try {
			return nextImpl();
		} finally {
			lock.unlock();
		}
	}
	
	protected abstract K nextImpl();
	
}
