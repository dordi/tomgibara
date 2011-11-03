package com.tomgibara.crinch.record;


public class ParsedRecord extends AbstractRecord implements LinearRecord {

	private final ColumnParser parser;
	private final StringRecord record;
	private final int length;
	int index = 0;
	boolean nullFlag;
	IllegalArgumentException invalidCause;

	@Override
	public boolean hasNext() {
		return index < length;
	}
	
	public ParsedRecord(ColumnParser parser, StringRecord record) {
		super(record);
		if (parser == null) throw new IllegalArgumentException("null parser");
		if (record == null) throw new IllegalArgumentException("null record");
		this.parser = parser;
		this.record = record;
		this.length = record.length();
	}
	
	@Override
	public String nextString() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseString(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return null;
	}
	
	@Override
	public char nextChar() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseChar(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return '\0';
	}
	
	@Override
	public boolean nextBoolean() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseBoolean(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return false;
	}
	
	@Override
	public byte nextByte() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseByte(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return (byte) 0;
	}
	
	@Override
	public short nextShort() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseShort(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return (short) 0;
	}
	
	@Override
	public int nextInt() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseInt(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return 0;
	}
	
	@Override
	public long nextLong() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseLong(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return 0L;
	}
	
	@Override
	public float nextFloat() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseFloat(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return 0.0f;
	}
	
	@Override
	public double nextDouble() {
		String next = next();
		if (next == null) {
			nullFlag = true;
		} else {
			try {
				return parser.parseDouble(next);
			} catch (IllegalArgumentException e) {
				invalidCause = e;
			}
		}
		return 0.0;
	}
	
	@Override
	public void skipNext() {
		if (index == length) throw new IllegalStateException("fields exhausted");
		index++;
	}

	@Override
	public boolean wasInvalid() {
		if (index == 0) throw new IllegalStateException("no field read");
		return invalidCause != null;
	}
	
	@Override
	public boolean wasNull() {
		if (index == 0) throw new IllegalStateException("no field read");
		return nullFlag;
	}
	
	@Override
	public void exhaust() {
		index = length;
	}
	
	private String next() {
		if (index == length) throw new IllegalStateException();
		nullFlag = false;
		invalidCause = null;
		String str = record.get(index++);
		return str != null && str.isEmpty() ? null : str;
	}
	
}
