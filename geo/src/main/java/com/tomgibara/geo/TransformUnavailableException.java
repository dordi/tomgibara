package com.tomgibara.geo;

public class TransformUnavailableException extends RuntimeException {

	private static final long serialVersionUID = -3983054942122984072L;

	public TransformUnavailableException() {
	}

	public TransformUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransformUnavailableException(String message) {
		super(message);
	}

	public TransformUnavailableException(Throwable cause) {
		super(cause);
	}

}
