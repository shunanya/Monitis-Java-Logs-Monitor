package org.monitis.logmonitor.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.monitis.logmonitor.logger.LogWriter;
import org.monitis.logmonitor.utils.Config;

public class LogServer extends Thread {

	private static String host = Config.getConfigStringValue("server.host", "localhost");
	private static int port = Config.getConfigIntValue("server.port", 4560);
    private Logger logger =Logger.getLogger("log_server");
	private LogWriter writer;

	public LogServer(LogWriter writer) {
		this.writer = writer;
	}

	@Override
	public void run() {
		logger.debug("DEBUG: Listening on port " + port);
		ServerSocket server = null;
		try {
			server = new ServerSocket(port, 1000);// create ServerSocket
		} catch (IOException e1) {
			logger.debug("DEBUG: Exception while creating server socket - " + e1.getMessage());
			Thread.currentThread().interrupt();
		}
		while (true) {
			try {
				// wait for a connection
				logger.debug("Waiting to accept a new client.");

				Socket connection = server.accept(); // connection to client

				InetAddress iad = connection.getInetAddress();

				logger.debug("Connected to client at " + connection.getInetAddress());
				logger.debug("Starting new logger node.");

				if (iad.getHostName().equalsIgnoreCase(host)) {
					new LogNode(connection, writer).start();
				} else {
					logger.warn("Unknown client is connected - rejecting.");
				}
			} catch (Exception e) {
				logger.warn("Server terminated connection (" + e.getMessage() + ")");
			}
		}
	}

	public static void main(String[] args) {
		new LogServer(null).start();
	}
}
