package com.tomgibara.crinch.bits;

public class BitStreamException extends RuntimeException {

	private static final long serialVersionUID = 6076037872218957434L;

	public BitStreamException() {
	}

	public BitStreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public BitStreamException(String message) {
		super(message);
	}

	public BitStreamException(Throwable cause) {
		super(cause);
	}
	
}
