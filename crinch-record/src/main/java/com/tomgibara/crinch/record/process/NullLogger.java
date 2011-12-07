package com.tomgibara.crinch.record.process;

public class NullLogger implements ProcessLogger {

	@Override
	public void log(String message) {
	}

	@Override
	public void log(Level level, String message) {
	}

	@Override
	public void log(String message, Throwable t) {
	}

	@Override
	public void log(Level level, String message, Throwable t) {
	}

}
