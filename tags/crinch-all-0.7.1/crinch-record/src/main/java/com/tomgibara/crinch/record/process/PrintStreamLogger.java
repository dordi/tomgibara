package com.tomgibara.crinch.record.process;

import java.io.PrintStream;

public class PrintStreamLogger implements ProcessLogger {

	private final PrintStream out;
	private final PrintStream err;
	private Level level = Level.INFO;
	
	public PrintStreamLogger() {
		this(System.out, System.err);
	}

	public PrintStreamLogger(PrintStream out) {
		this(out, out);
	}
	
	public PrintStreamLogger(PrintStream out, PrintStream err) {
		if (out == null) throw new IllegalArgumentException("null out");
		if (err == null) throw new IllegalArgumentException("null err");
		this.out = out;
		this.err = err;
	}
	
	// accessors
	
	public Level getLevel() {
		return level;
	}
	
	public void setLevel(Level level) {
		if (level == null) throw new IllegalArgumentException("null level");
		this.level = level;
	}
	
	// logger methods
	
	@Override
	public void log(String message) {
		log(Level.INFO, message);
	}
	
	@Override
	public void log(Level level, String message) {
		if (level == null) throw new IllegalArgumentException("null level");
		if (message == null) return;
		if (level.ordinal() < this.level.ordinal()) return;
		out.println(message);
	}

	@Override
	public void log(String message, Throwable t) {
		log(Level.ERROR, message, t);
	}
	
	@Override
	public void log(Level level, String message, Throwable t) {
		if (level == null) throw new IllegalArgumentException("null level");
		if (level.ordinal() < this.level.ordinal()) return;
		if (message != null) err.println(message);
		if (t != null) t.printStackTrace(err);
	}

}
