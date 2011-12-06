package com.tomgibara.crinch.record.process;

public interface ProcessLogger {

	public enum Level {
		DEBUG,
		INFO,
		WARN,
		ERROR,
	}
	
	void log(String message);
	
	void log(Level level, String message);
	
	void log(String message, Throwable t);

	void log(Level level, String message, Throwable t);

}
