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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StuppLock implements Lock {

	static final StuppLock NOOP_LOCK = new StuppLock(null);
	
	private final ReentrantLock lock;
	
	private StuppLock(ReentrantLock lock) {
		this.lock = lock;
	}
	
	public StuppLock(boolean fair) {
		this( new ReentrantLock(false) );
	}
	
	public StuppLock() {
		this(false);
	}

	@Override
	public void lock() {
		if (lock != null) lock.lock();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		if (lock != null) lock.lockInterruptibly();
	}

	@Override
	public Condition newCondition() {
		return lock == null ? null : lock.newCondition();
	}

	@Override
	public boolean tryLock() {
		return lock == null ? true : lock.tryLock();
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return lock == null ? true : lock.tryLock(time, unit);
	}

	@Override
	public void unlock() {
		if (lock != null) lock.unlock();
	}

}
