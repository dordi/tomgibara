package com.tomgibara.beamtail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.tomgibara.pronto.config.ConfigFactory;
import com.tomgibara.pronto.config.ConfigSource;
import com.tomgibara.pronto.util.Duration;
import com.tomgibara.pronto.util.Streams;

//TODO change conf file to not be a flag, perhaps a list of filenames
//TODO introduce logging
//TODO limit number of connections
//TODO remove dependencies on files
public class Beamtail implements Runnable {

	public static final String DEFAULT_CONFIG_PATH = "beamtail.conf";
	public static final File DEFAULT_CONFIG_FILE = new File(DEFAULT_CONFIG_PATH);
	public static final String DEFAULT_SETTINGS_PATH = "beamtail.settings";
	public static final File DEFAULT_SETTINGS_FILE = new File(DEFAULT_SETTINGS_PATH);
	
	private static void message(String type, String message, Throwable t) {
		type = type.toUpperCase();
		if (type.length() > 7) {
			type = type.substring(7);
		} else if (type.length() < 7) {
			type = String.format("% 7s", type);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(' ').append(message);
		if (t != null) sb.append(": ").append(t.getMessage());
		System.err.println(sb);
	}

	private static void error(String message, Throwable t) {
		message("ERROR  ", message, t);
	}

	private static void warning(String message, Throwable t) {
		message("WARNING", message, t);
	}

	public static void main(String[] args) throws IOException {
		new Beamtail(args).run();
	}

	interface Settings {

		File getStorageFile();
		
		Boolean isResumeOnly();
		
		Boolean isResumeOnError();
		
		Integer getQueueLimit();
		
		Duration getWriteDelay();
		
		Duration getConnectionTimeout();
		
		Duration getShutdownTimeout();
		
	}
	
	private static class DefaultSettings implements Settings {

		static final Duration WRITE_DELAY = new Duration(10000L);
		static final Duration CONNECTION_TIMEOUT = new Duration(10000L);
		static final Boolean RESUME_ONLY = Boolean.FALSE;
		static final Boolean RESUME_ON_ERROR = Boolean.FALSE;
		static final Integer QUEUE_LIMIT = 20480;
		static final Duration SHUTDOWN_TIMEOUT = new Duration(2000L);
		static final File STORAGE_FILE = new File("beamtail.storage");
		
		private final Settings settings;
		
		DefaultSettings(Settings settings) {
			this.settings = settings;
		}
	
		@Override
		public File getStorageFile() {
			File file = settings.getStorageFile();
			return file == null ? STORAGE_FILE : file;
		}
		
		@Override
		public Duration getConnectionTimeout() {
			Duration duration = settings.getConnectionTimeout();
			return duration == null ? CONNECTION_TIMEOUT : duration;
		}

		@Override
		public Duration getWriteDelay() {
			Duration duration = settings.getWriteDelay();
			return duration == null ? WRITE_DELAY : duration;
		}

		@Override
		public Boolean isResumeOnly() {
			Boolean b = settings.isResumeOnly();
			return b == null ? RESUME_ONLY : b;
		}
		
		@Override
		public Boolean isResumeOnError() {
			Boolean b = settings.isResumeOnError();
			return b == null ? RESUME_ON_ERROR : b;
		}
		
		@Override
		public Integer getQueueLimit() {
			Integer value = settings.getQueueLimit();
			return value == null || value < 1 ? QUEUE_LIMIT : value;
		}
		
		@Override
		public Duration getShutdownTimeout() {
			Duration duration = settings.getShutdownTimeout();
			return duration == null ? SHUTDOWN_TIMEOUT : duration;
		}
		
	}

	public static class FileConverter implements IStringConverter<File> {
		@Override
		public File convert(String value) {
			return new File(value);
		}
	}
	
	private final FileSystem fs = FileSystems.getDefault();
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
	private final Tailer tailer = new Tailer();
	private final Beamer beamer = new Beamer();
	private final Writer writer = new Writer();
	
	@Parameter(names={"-c", "--config"}, converter=FileConverter.class, description="path to configuration file", arity=1)
	private File configFile = DEFAULT_CONFIG_FILE;
	@Parameter(names={"-s", "--settings"}, converter=FileConverter.class, description="path to settings file", arity=1)
	private File settingsFile = DEFAULT_SETTINGS_FILE;
	@Parameter(names={"-f", "--files"}, converter=FileConverter.class, description="file to which file information is saved", arity=1)
	private Settings settings;
	
	private final Set<Path> paths = new HashSet<Path>();
	private final Map<PathMatcher, InetSocketAddress> addresses = new LinkedHashMap<PathMatcher, InetSocketAddress>();

	private final Set<Path> modifiedPaths = new LinkedHashSet<Path>();
	private final Map<Path, Queue> queues = new HashMap<Path, Queue>();
	private final Map<Path, Connection> connections = new HashMap<Path, Connection>(); // kept independently of queues because they may outlive them

	private WatchService watcher = null; // non-null when running
	private Map<Path, FileSize> fileSizes = new HashMap<Path, FileSize>(); // persisted file size information

	public Beamtail(String[] args) throws IOException {
		new JCommander(this, args);
		try {
			configure();
			settings = new DefaultSettings( ConfigFactory.getInstance().newConfig(new SettingsSource(), null).adaptSettings(null, false, Settings.class) );
		} catch (RuntimeException e) {
			error("Failed to configure", e);
			System.exit(1);
		}
		
	}

	private void configure() {
		paths.clear();
		addresses.clear();

		if (!configFile.isFile()) throw new IllegalArgumentException("Cannot read configuration file: " + configFile);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			try {
				int lineNo = 0;
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					lineNo ++;
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.startsWith("#")) continue;
					int i  = line.lastIndexOf(' ');
					if (i == -1) throw new IllegalArgumentException("Line " + lineNo + ": invalid line");
					String address = line.substring(i + 1);
					int j = address.indexOf(':');
					if (j == -1) throw new IllegalArgumentException("Line " + lineNo + ": no port number in address " + address);
					String portStr = address.substring(j + 1);
					int port;
					try {
						port = Integer.parseInt(portStr);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Line " + lineNo + ": port not a number " + portStr);
					}
					if (port < 1 || port > 65535) throw new IllegalArgumentException("Line " + lineNo + ": invalid port number " + port);
					String host = address.substring(0, j);
					InetAddress inetAddress;
					try {
						inetAddress = InetAddress.getByName(host);
					} catch (UnknownHostException e) {
						throw new IllegalArgumentException("Line " + lineNo + ": unknown host " + host);
					}
					InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
					
					String glob = line.substring(0, i).trim();
					if (glob.length() > 1) {
						char first = glob.charAt(0);
						char last = glob.charAt(glob.length() - 1);
						if (first == last && (first == '\'' || first == '"')) {
							glob = glob.substring(1, glob.length() - 1);
						}
					}
					PathMatcher matcher;
					try {
						matcher = fs.getPathMatcher("glob:" + glob);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("Line " + lineNo + ": invalid glob syntax " + glob);
					}

					int k = glob.indexOf('*');
					int l = glob.indexOf('?');
					String pathStr;
					if (k == -1 && l == -1) {
						pathStr = glob;
					} else if (k == -1) {
						pathStr = glob.substring(0, l);
					} else if (l == -1) {
						pathStr = glob.substring(0, k);
					} else {
						pathStr = glob.substring(0, Math.min(k, l));
					}
					
					Path path;
					try {
						path = fs.getPath(pathStr);
						if (!pathStr.endsWith("/")) path = path.getParent();
					} catch (InvalidPathException e) {
						throw new IllegalArgumentException("Line " + lineNo + ": invalid path syntax " + pathStr);
					}
					
					paths.add(path);
					addresses.put(matcher, socketAddress);
				}
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					/* ignored */
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading configuration file: " + configFile, e);
		}
		
		outer: do {
			for (Path path : paths) {
				for (Path other : paths) {
					if (other == path) continue;
					if (path.startsWith(other)) {
						paths.remove(path);
						continue outer;
					}
				}
			}
			break outer;
		} while (true);

	}

	private void execute(Runnable command) {
		//TODO minor race condition here
		if (!executor.isShutdown()) {
			executor.execute(command);
		}
	}
	
	private ScheduledFuture<?> schedule(Runnable command, Duration duration) {
		//TODO minor race condition here
		if (executor.isShutdown()) return null;
		return executor.schedule(command, duration.getTime(), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void run() {
		if (watcher != null) return;
		
		// read previous state
		boolean exit = resume();
		if (exit) System.exit(1);
		synchronized (modifiedPaths) {
			modifiedPaths.addAll(fileSizes.keySet());
			executor.execute(tailer);
		}

		// create the watcher
		try {
			watcher = fs.newWatchService();
		} catch (UnsupportedOperationException e) {
			error("File watching not supported by file system", e);
			return;
		} catch (IOException e) {
			error("Failed to create file watcher", e);
			return;
		}

		Hook hook = null;
		try {
			// register the paths to watch
			Kind<?>[] kinds = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW};
			for (Path path : paths) {
				try {
					path.register(watcher, kinds);
				} catch (IOException e) {
					warning("Unable to watch path", e);
					return;
				}
			}
			
			// initiate files writer and register a shutdown hook
			hook = new Hook();
			Runtime.getRuntime().addShutdownHook(hook);
			writer.schedule();
			
			// event loop
			while (true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException e) {
					break;
				}
	
				for (WatchEvent<?> event : key.pollEvents()) {
					if (event.kind().equals(StandardWatchEventKinds.OVERFLOW)) {
						warning("Filesystem event overflow", null);
						continue;
					}
					Path path = (Path) event.context();
					Path parent = (Path) key.watchable();
					Path fullPath = parent.resolve(path);
					boolean added;
					synchronized (modifiedPaths) {
						added = modifiedPaths.add(fullPath);
					}
					if (added) execute(tailer);
				}
	
				key.reset();
			}
			
		} finally {
			// close watcher
			try {
				watcher.close();
			} catch (IOException e) {
				warning("Could not close watcher", e);
			} finally {
				watcher = null;
				try {
					Runtime.getRuntime().removeShutdownHook(hook);
				} catch (IllegalStateException e) {
					/* ignored - we have no way to test for this condition */
				}
			}
		}
	}

	/**
	 * Provides the configuration properties for settings.
	 */
	
	private class SettingsSource implements ConfigSource {

		@Override
		public Map<String, String> getProperties() throws RuntimeException {
			if (!settingsFile.isFile()) return Collections.emptyMap();
			Properties properties = new Properties();
	        InputStream in = null;
	        try {
	            in = new BufferedInputStream(new FileInputStream(settingsFile));
				properties.load(in);
	        } catch (IOException e) {
	            throw new RuntimeException("Error reading settings file: " + settingsFile, e);
	        } finally {
	            Streams.safeClose(in);
	        }
			return new HashMap<String, String>((Map) properties);
		}

		@Override
		public long lastModified() throws RuntimeException {
	        long time = settingsFile.lastModified();
	        return time == 0L ? System.currentTimeMillis() : time;
		}
	
	}

	/**
	 * Copies new bytes from the tail of a file into a queue.
	 */
	
	private class Tailer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				// find a modified file path
				final Path path;
				synchronized (modifiedPaths) {
					if (modifiedPaths.isEmpty()) return;
					//TODO yuk!
					Iterator<Path> i = modifiedPaths.iterator();
					path = i.next();
					i.remove();
				}
				File file = path.toFile();
				if (file.isDirectory()) continue;

				// find an address for it
				InetSocketAddress address = null;
				Set<Entry<PathMatcher,InetSocketAddress>> set = addresses.entrySet();
				for (Entry<PathMatcher, InetSocketAddress> entry : set) {
					if (entry.getKey().matches(path)) {
						address = entry.getValue();
						break;
					}
				}
				if (address == null) continue;

				// record the change in file size
				long newLength = file.length();
				boolean deleted = newLength == 0L && !file.exists();
				//long oldLength;
				long readLength;
				synchronized (fileSizes) {
					if (deleted) {
						fileSizes.remove(path);
						readLength = -1L;
					} else {
						FileSize size = fileSizes.get(path);
						if (size == null) {
							size = new FileSize();
							fileSizes.put(path, size);
						}
						readLength = size.getRead();
						size.setAvailable(newLength);
					}
				}
				
				if (!deleted) {
					if (newLength <= readLength) return;

					// obtain the queue
					Queue queue;
					//TODO path should be absolute?
					synchronized (queues) {
						queue = queues.get(path);
						if (queue == null) {
							queue = new Queue(path, address);
							queues.put(path, queue);
						}
					}

					// see how much we are permitted to read
					int length = queue.commit(newLength - readLength);
					if (length == 0) return;
					
					// read the bytes
					byte[] bytes = new byte[length];
					RandomAccessFile raf = null;
					try {
						raf = new RandomAccessFile(file, "r");
						raf.seek(readLength);
						raf.readFully(bytes);
						synchronized (fileSizes) {
							FileSize size = fileSizes.get(path);
							if (size != null) size.setRead(size.getRead() + length);
						}
					} catch (IOException e) {
						warning("Failed to read file: " + path, e);
						continue;
					} finally {
						try {
							raf.close();
						} catch (IOException e) {
							/* ignored */
						}
					}
					
					// enqueue the bytes and schedule a beam
					synchronized (queues) {
						queue.enqueue(bytes);
					}
					execute(beamer);
				}
			}
		}
		
	}

	/**
	 * Sends the bytes in a queue to a connection.
	 */
	
	private class Beamer implements Runnable {

		private final Set<Queue> owned = new HashSet<Queue>();
		
		@Override
		public void run() {
			main: while (true) {
				Queue queue = null;
				
				// take ownership of a queue
				synchronized (queues) {
					for (Queue q : queues.values()) {
						if (owned.contains(q)) continue;
						queue = q;
						owned.add(q);
						break;
					}
					if (queue == null) return;
				}

				while (true) {

					// obtain bytes from queue
					byte[] bytes;
					synchronized (queues) {
						if (queue.isRedundant()) {
							queues.remove(queue.path);
							owned.remove(queue);
							continue main;
						} else {
							bytes = queue.flush();
						}
					}

					// obtain a connection for the queue
					Connection connection; 
					synchronized (connections) {
						connection = connections.get(queue.path);
						if (connection == null) {
							connection = new Connection(queue.path, queue.address);
							connections.put(queue.path, connection);
						}
					}
					
					//TODO is there a way to have the connection neatly put the bytes from the queue?
					// send the bytes
					try {
						connection.send(bytes);
					} catch (IOException e) {
						warning("Failed to send data to " + connection.address, e);
					}
				}
				
			}
		}
		
	}

	/**
	 * Records which file bytes have already been sent to a storage file.
	 */
	
	private class Writer implements Runnable {

		private final Map<File, Properties> previous = new HashMap<File, Properties>();
		
		void schedule() {
			if (watcher == null) return; // no point attempting to write file information if not running
			Beamtail.this.schedule(this, settings.getWriteDelay());
		}

		@Override
		public synchronized void run() {
			try {
				// determine file to save to
				File file = settings.getStorageFile();
				
				// build properties to save
				Properties  properties = new Properties();
				synchronized (fileSizes) {
					Set<Entry<Path,FileSize>> entrySet = fileSizes.entrySet();
					for (Entry<Path, FileSize> entry : entrySet) {
						properties.put(entry.getKey().toString(), Long.toString(entry.getValue().getSent()));
					}
				}
				
				// bail if properties have not changed
				Properties old = previous.get(file);
				if (old != null && old.equals(properties)) return;
	
				// save the properties to the file
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					properties.store(out, "Beamtail");
					previous.put(file, properties);
				} catch (IOException e) {
					warning("Failed to save file state", e);
				} finally {
					Streams.safeClose(out);
				}
			} finally {
				// run again if needed
				schedule();
			}
		}
		
	}

	// return true on failure
	private boolean resume() {
		File file = settings.getStorageFile();
		if (!file.isFile()) {
			if (settings.isResumeOnly()) {
				error("No storage to resume from", null);
				return true;
			} else {
				return false;
			}
		}
		Properties  properties = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			properties.load(in);
		} catch (IOException e) {
			if (settings.isResumeOnError()) {
				warning("Failed to restore state", e);
			} else {
				error("Failed to restore state", e);
				return true;
			}
		} finally {
			Streams.safeClose(in);
		}
		synchronized (fileSizes) {
			Set<Entry<String, String>> entries = (Set) properties.entrySet();
			for (Entry<String, String> entry : entries) {
				Path path = fs.getPath(entry.getKey());
				long read = -1L;
				if (Files.exists(path)) {
					try {
						read = Long.parseLong(entry.getValue());
					} catch (NumberFormatException e) {
						String message = "Invalid file size: " + entry.getKey() + "=" + entry.getValue();
						if (settings.isResumeOnError()) {
							warning(message, null);
						} else {
							error(message, null);
							return true;
						}
					}
				}
				if (read < 0L) {
					fileSizes.remove(path);
				} else {
					FileSize fileSize = fileSizes.get(path);
					if (fileSize == null) {
						fileSize = new FileSize();
						fileSizes.put(path, fileSize);
					}
					try {
						fileSize.setAvailable(Files.size(path));
					} catch (IOException e) {
						fileSizes.remove(path);
					}
					fileSize.setRead(read);
					fileSize.setSent(read);
				}
			}
		}
		return false;
	}

	/**
	 * A shutdown hook that prompts swift termination.
	 */
	
	private class Hook extends Thread {
		
		private final Thread main;
		
		Hook() {
			main = Thread.currentThread();
		}
		
		@Override
		public void run() {
			Duration timeout = settings.getShutdownTimeout();
			try {
				// kick the main thread to stop the flow of runnables to the executor
				main.interrupt();
				// stop the executor, this will interrupt any ongoing IO too
				executor.shutdownNow();
				// wait for an orderly shutdown of the executor
				boolean okay = executor.awaitTermination(timeout.getTime(), TimeUnit.MILLISECONDS);
				if (!okay) warning("Shutdown exceeded timeout: " + timeout, null);
			} catch (InterruptedException e) {
				warning("Shutdown interrupted", e);
			} finally {
				// finally attempt to write the file sizes
				writer.run();
			}
		}
		
	}

	/**
	 * Accumulates the bytes to be send down a connection
	 */
	
	private class Queue {
		
		final Path path;
		final InetSocketAddress address;
		private int committed = 0;
		ByteArrayOutputStream out = null;

		public Queue(Path path, InetSocketAddress address) {
			this.path = path;
			this.address = address;
		}
		
		int getCommitLimit() {
			return settings.getQueueLimit();
		}
		
		int commit(long count) {
			int sum = (int) Math.min(getCommitLimit(), committed + count);
			int limit = sum - committed;
			committed = sum;
			return limit;
		}
		
		boolean isRedundant() {
			return committed == 0;
		}
		
		int size() {
			return out == null ? 0 : out.size();
		}
		
		void enqueue(byte[] bytes) {
			if (bytes.length + size() > committed) throw new IllegalStateException("Committed " + committed + " on " + path + " but attempted to store " + bytes.length + " in addition to " + out.size() );
			if (out == null) out = new ByteArrayOutputStream(bytes.length);
			try {
				out.write(bytes);
			} catch (IOException e) {
				/* ignored - not possible */
			}
		}
		
		byte[] flush() {
			byte[] bytes;
			if (out == null) {
				bytes = new byte[0];
			} else {
				bytes = out.toByteArray();
				out = null;
			}
			committed -= bytes.length;
			return bytes;
		}

		@Override
		public String toString() {
			return "[" + size() +"," + committed + "]";
		}
		
	}
	
	/**
	 * Used to send bytes to a socket.
	 */
	
	private class Connection {

		final Path path;
		final InetSocketAddress address;
		final Duration timeout;
		
		private long lastAccess;
		private SocketChannel channel;
		private final Runnable shutdown = new Runnable() {
			@Override
			public void run() {
				shutdown();
			}
		};

		private ScheduledFuture<?> future;
		
		Connection(Path path, InetSocketAddress address) {
			this.path = path;
			this.address = address;
			timeout = settings.getConnectionTimeout();
		}
		
		void send(byte[] bytes) throws IOException {
			if (channel == null) connect();
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			while (buffer.hasRemaining()) {
				try {
					send(buffer);
				} catch (IOException e) {
					disconnect();
					connect();
				}
			}
		}

		private void connect() throws IOException {
			channel = SocketChannel.open();
			//TODO how to timeout?
			channel.connect(address);
		}
		
		private void disconnect() {
			try {
				channel.close();
				channel = null;
			} catch (IOException e) {
				/* ignored */
			}
		}
		
		private void send(ByteBuffer buffer) throws IOException {
			FileSize size;
			synchronized (fileSizes) {
				size = fileSizes.get(path);
			}
			while (buffer.hasRemaining()) {
				lastAccess = System.currentTimeMillis();
				scheduleShutdown();
				int sent = channel.write(buffer);
				boolean incomplete;
				synchronized (fileSizes) {
					size.setSent(size.getSent() + sent);
					incomplete = size.getRead() < size.getAvailable();
				}
				// we may not have had a large enough queue, nudge the tailer again if necessary
				if (incomplete) {
					synchronized (modifiedPaths) {
						modifiedPaths.add(path);
					}
					execute(tailer);
				}
			}
		}

		private synchronized void shutdown() {
			if (channel == null) return;
			if (System.currentTimeMillis() - lastAccess > timeout.getTime()) {
				disconnect();
			} else {
				scheduleShutdown();
			}
		}
		
		private synchronized void scheduleShutdown() {
			if (future != null) {
				future.cancel(true);
			}
			future = schedule(shutdown, timeout);
		}
		
		@Override
		public String toString() {
			return path + "->" + address;
		}
	}

	/**
	 * A struct that records the last known file size, and the number of bytes that were sent
	 */
	
	private static class FileSize {
		
		//invariant: sent < read < available
		private long sent;
		private long read;
		private long available;
		
		long getAvailable() {
			return available;
		}
		
		void setAvailable(long available) {
			this.available = available;
			setRead(read);
		}

		long getRead() {
			return read;
		}
		
		void setRead(long read) {
			read = Math.min(available, read);
			this.read = read;
			setSent(sent);
		}

		long getSent() {
			return sent;
		}
		
		void setSent(long sent) {
			sent = Math.min(read, sent);
			this.sent = sent;
		}
		
		@Override
		public String toString() {
			return "S:" + sent + "/R:" + read + "/A:" + available;
		}
		
	}

}
