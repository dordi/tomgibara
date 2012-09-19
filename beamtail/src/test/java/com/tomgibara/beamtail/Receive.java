package com.tomgibara.beamtail;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Receive {

	public static void main(String[] args) throws IOException, InterruptedException {
		try (ServerSocket socket = new ServerSocket(Integer.parseInt(args[0]))) {
			while (true) {
				try {
					Socket s = socket.accept();
					new Worker(s).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class Worker extends Thread {

		private final Socket socket;
		
		Worker(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				InputStream in = socket.getInputStream();
				while (true) {
					int b = in.read();
					if (b < 0) {
						System.err.print("EOS");
						System.err.flush();
						break;
					}
					System.out.write(b);
					System.out.flush();
				}
				in.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		
	}
	
}
