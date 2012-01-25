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

public abstract class AbstractLinearRecord extends AbstractRecord implements LinearRecord {

	private int index = 0;
	private int mark = Integer.MAX_VALUE;
	private boolean wasNull;
	private boolean wasInvalid;
	private boolean flagsSet = false;
	
	protected abstract Object getValue(int index, boolean free);

	protected abstract int getLength();

	protected AbstractLinearRecord(long ordinal, long position) {
		super(ordinal, position);
	}
	
	@Override
	public boolean hasNext() {
		return index < getLength();
	}

	@Override
	public String nextString() {
		Object value = nextValue();
		if (value != null) try {
			return (String) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return null;
	}

	@Override
	public char nextChar() {
		Object value = nextValue();
		if (value != null) try {
			return (Character) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return '\0';
	}

	@Override
	public boolean nextBoolean() {
		Object value = nextValue();
		if (value != null) try {
			return (Boolean) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return false;
	}

	@Override
	public byte nextByte() {
		Object value = nextValue();
		if (value != null) try {
			return (Byte) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return 0;
	}

	@Override
	public short nextShort() {
		Object value = nextValue();
		if (value != null) try {
			return (Short) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return 0;
	}

	@Override
	public int nextInt() {
		Object value = nextValue();
		if (value != null) try {
			return (Integer) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return 0;
	}

	@Override
	public long nextLong() {
		Object value = nextValue();
		if (value != null) try {
			return (Long) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return 0L;
	}

	@Override
	public float nextFloat() {
		Object value = nextValue();
		if (value != null) try {
			return (Float) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return 0.0f;
	}

	@Override
	public double nextDouble() {
		Object value = nextValue();
		if (value != null) try {
			return (Double) value;
		} catch (ClassCastException e) {
			wasInvalid = true;
		}
		return 0.0;
	}

	@Override
	public void skipNext() {
		if (index == getLength()) throw new IllegalStateException("record exhausted");
		index++;
		flagsSet = false;
	}

	@Override
	public boolean wasNull() {
		if (!flagsSet) throw new IllegalStateException("no value returned");
		return wasNull;
	}

	@Override
	public boolean wasInvalid() {
		if (!flagsSet) throw new IllegalStateException("no value returned");
		return wasInvalid;
	}

	@Override
	public void mark() {
		mark = index;
	}

	@Override
	public void reset() {
		if (mark == Integer.MAX_VALUE) throw new IllegalStateException("not marked");
		index = mark;
		flagsSet = false;
	}

	@Override
	public void release() {
		index = getLength();
	}

	private Object nextValue() {
		if (index == getLength()) throw new IllegalStateException("record exhausted");
		wasInvalid = false;
		Object value = getValue(index, index < mark);
		index ++;
		wasNull = value == null;
		flagsSet = true;
		return value;
	}
	
}
