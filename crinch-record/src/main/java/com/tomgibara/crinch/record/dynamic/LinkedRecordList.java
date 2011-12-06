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
package com.tomgibara.crinch.record.dynamic;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.NoSuchElementException;

//TODO could implement more methods directly for better performance
public class LinkedRecordList<R extends LinkedRecord> extends AbstractSequentialList<R> {

	// fields
	
	private R head = null;

	// constructors
	
	public LinkedRecordList() { }
	
	public LinkedRecordList(Collection<R> collection) {
		if (collection == null) throw new IllegalArgumentException("null collection");
		addAll(collection);
	}
	
	// required method implementations
	
	@Override
	public ListIterator<R> listIterator(int index) {
		return new LinkedIterator(index);
	}

	@Override
	public int size() {
		if (head == null) return 0;
		int size = 0;
		LinkedRecord next = head;
		do {
			size ++;
			next = next.getNextRecord();
		} while (next != head);
		return size;
	}
	
	// improved implementations

	@Override
	public boolean isEmpty() {
		return head == null;
	}
	
	@Override
	public void clear() {
		while (head != null) {
			LinkedRecord next = head.getNextRecord();
			if (next == head) {
				head = null;
			} else {
				head.removeRecord();
				next = head;
			}
		}
	}

	@Override
	public boolean add(R record) {
		if (record == null) throw new IllegalArgumentException("null record");
		record.removeRecord();
		if (head == null) {
			head = record;
		} else {
			record.insertRecordBefore(head);
		}
		return true;
	}
	
	// inner classes
	
	private class LinkedIterator implements ListIterator<R> {

		private int nextIndex = 0;
		private R nextRecord = head;
		private R record = null;
		
		public LinkedIterator(int index) {
			if (index < 0) throw new IndexOutOfBoundsException("negative index");
			while (index-- > 0) {
				if (!hasNext()) throw new IndexOutOfBoundsException("index exceeds size");
				next();
			}
		}
		
		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public int previousIndex() {
			return nextIndex  - 1;
		}

		@Override
		public boolean hasNext() {
			return nextRecord != null;
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public R next() {
			if (!hasNext()) throw new NoSuchElementException("no next record");
			record = nextRecord;
			nextRecord = (R) nextRecord.getNextRecord();
			if (nextRecord == head) nextRecord = null;
			return record;
		}

		@Override
		public R previous() {
			if (!hasPrevious()) throw new NoSuchElementException("no previous record");
			nextRecord = record = (R) nextRecord.getPreviousRecord();
			return nextRecord;
		}

		@Override
		public void add(R record) {
			if (record == null) throw new IllegalArgumentException("null record");
			record.removeRecord();
			if (head == null) {
				head = record;
			} else if (nextRecord == null) {
				record.insertRecordBefore(head);
			} else {
				record.insertRecordBefore(nextRecord);
				if (nextRecord == head) head = record;
			}
			nextRecord = record;
			this.record = null;
		}

		@Override
		public void remove() {
			if (record == null) throw new IllegalStateException("no record to remove");
			if (record == nextRecord) {
				nextRecord = (R) record.getNextRecord();
			}
			if (record == head) {
				head = (R) head.getNextRecord();
				if (record == head) {
					head = null;
					nextIndex = 0;
					nextRecord = null;
				}
			}
			record.removeRecord();
			record = null;
		}

		@Override
		public void set(R record) {
			if (record == null) throw new IllegalStateException("no record to set");
			if (record == head) head = record;
			if (record == nextRecord) nextRecord = record;
			record.removeRecord();
			record.replaceRecord(this.record);
			this.record = record;
		}
		
		
		
	}

}
