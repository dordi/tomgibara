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

import java.util.HashSet;

import com.tomgibara.stupp.StuppFactory;
import com.tomgibara.stupp.StuppLock;
import com.tomgibara.stupp.StuppScope;
import com.tomgibara.stupp.StuppType;

import junit.framework.TestCase;

public class LockTest extends TestCase {

	//TODO this is a weak test
	public void testScopeSynchronized() throws Exception {
		final StuppScope scope = StuppScope.newDefinition().setLock(new StuppLock()).createScope();
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope);
		final Thread t1 = new Thread(new KeyedCreator<Book>(bookFactory, 0, 2, 1000));
		final Thread t2 = new Thread(new KeyedCreator<Book>(bookFactory, 1, 2, 1000));
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
	
	public void testSequenceSynchronized() throws Exception {
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppFactory<Book, Long> factory1 = new StuppFactory<Book, Long>(type, StuppScope.newDefinition().createScope());
		final StuppFactory<Book, Long> factory2 = new StuppFactory<Book, Long>(type, StuppScope.newDefinition().createScope());
		LongSequence sequence = new LongSequence(new StuppLock());
		factory1.setSequence(sequence);
		factory2.setSequence(sequence);
		final int count = 10000;
		final Thread t1 = new Thread(new SequenceCreator<Book>(factory1, count));
		final Thread t2 = new Thread(new SequenceCreator<Book>(factory2, count));
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
		HashSet<Long> check = new HashSet<Long>();
		
		for (Book book : factory1.getAllInstances()) {
			assertTrue("Duplicate id: " + book.getId(), check.add(book.getId()));
		}
		for (Book book : factory2.getAllInstances()) {
			assertTrue("Duplicate id: " + book.getId(), check.add(book.getId()));
		}
		
		assertEquals(count*2, check.size());
	}
	
	private static class KeyedCreator<T> implements Runnable {
		
		private final StuppFactory<T, Long> factory;
		private final int count;
		private int next;
		private int step;

		public KeyedCreator(StuppFactory<T, Long> factory, int next, int step, int count) {
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
	
	private static class SequenceCreator<T> implements Runnable {

		private final StuppFactory<T, ?> factory;
		private final int count;

		public SequenceCreator(StuppFactory<T, ?> factory, int count) {
			this.factory = factory;
			this.count = count;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < count; i++) {
				factory.newInstance();
			}
		}
	}
	
	private static class LongSequence extends StuppSequence<Long> {

		public LongSequence(StuppLock lock) {
			super(new StuppSequence.Generator<Long>() {
				private long next = 0;
				@Override
				public Long next() {
					return next++;
				}
			}, lock);
		}
		
	}
	
}
