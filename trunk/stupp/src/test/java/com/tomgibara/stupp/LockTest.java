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

import com.tomgibara.stupp.StuppFactory;
import com.tomgibara.stupp.StuppLock;
import com.tomgibara.stupp.StuppScope;
import com.tomgibara.stupp.StuppType;

import junit.framework.TestCase;

public class LockTest extends TestCase {

	//TODO this is a weak test
	public void testSynchronization() throws Exception {
		final StuppScope scope = new StuppScope(new StuppLock());
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope);
		Thread t1 = new Thread(new Creator<Book>(bookFactory, 0, 2, 1000));
		Thread t2 = new Thread(new Creator<Book>(bookFactory, 1, 2, 1000));
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
	
	private static class Creator<T> implements Runnable {
		
		private final StuppFactory<T, Long> factory;
		private int next;
		private int step;
		private int count;

		public Creator(StuppFactory<T, Long> factory, int next, int step, int count) {
			this.factory = factory;
			this.next = next;
			this.step = step;
			this.count = count;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < count; i++) {
				factory.newInstance(new Long(next));
				factory.getAllInstances();
				next += step;
			}
		}
		
	}
	
}
