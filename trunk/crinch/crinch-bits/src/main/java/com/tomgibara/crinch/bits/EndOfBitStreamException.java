package com.tomgibara.crinch.bits;

public class EndOfBitStreamException extends BitStreamException {

	private static final long serialVersionUID = -4892414594243780142L;

	public EndOfBitStreamException() {
	}

	public EndOfBitStreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public EndOfBitStreamException(String message) {
		super(message);
	}

	public EndOfBitStreamException(Throwable cause) {
		super(cause);
	}

}
