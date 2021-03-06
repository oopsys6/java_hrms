package main.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;

public class Client {
	Socket socket;

	public Client(Socket socket) {
		this.socket = socket;
		run();
	}

	public void run() {
		Runnable thread = new Runnable() {
			SocketHandler socketHandler = Server.socketHandler;

			@Override
			public void run() {
				try {
					while (true) {
						String requestData = "";
						String responseData = "";

						requestData = receive();
						responseData = socketHandler.apiMapping(requestData);

						send(responseData);
					}
				} catch (Exception e) {
				}
			}

			private String receive() {
				String requestData = "";

				try {
					InputStream in = socket.getInputStream();
					byte[] buffer = new byte[512];
					int length = in.read(buffer);
					while (length == -1)
						throw new IOException();

					requestData = new String(buffer, 0, length, "UTF-8");

				} catch (Exception e) {
					try {
						System.out.println("[메시지 수신 오류] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());
					} catch (Exception e2) {
					}
				}

				return requestData;
			}

			private void send(String message) {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[메시지 송신 오류] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());
						Server.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {

					}
				}
			}
		};
		Server.threadPool.submit(thread);
	}

}