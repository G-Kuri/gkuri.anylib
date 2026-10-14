package gkuri.anylib.communication.tcp;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import gkuri.anylib.communication.CommunicationData;
import gkuri.anylib.communication.CommunicationEventListener;

public class TCPSender implements Closeable {
	
	private String host;
	private int port;

	private Socket socket;
	private BufferedWriter bw;
	
	private List<CommunicationEventListener> eventListeners = new LinkedList<>();

	public TCPSender(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void addEventListener(CommunicationEventListener listener) {
		if(listener == null) return;
		this.eventListeners.add(listener);
	}
	
	public boolean removeEventListener(CommunicationEventListener listener) {
		return this.eventListeners.remove(listener);
	}
	
	public synchronized boolean open() throws UnknownHostException, IOException {
		if(this.socket != null && !this.socket.isClosed()) return false;
		Socket socket = null;
		BufferedWriter bw = null;
		
		try {
			socket = new Socket(this.host, this.port);
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			
			this.socket = socket;
			this.bw = bw;
		} catch (Exception e) {
			if(socket != null) socket.close();
			if(bw != null) bw.close();
			throw e;
		}
		
		return true;
	}
	
	@Override
	public synchronized void close() throws IOException {
		if(bw == null) return;
		bw.close();
		socket.close();
		bw = null;
		socket = null;
	}
	
	public synchronized void send(CommunicationData<?> data) throws UnknownHostException, IOException {
		
		String json = CommunicationData.mapper.writeValueAsString(data);
		bw.write(json);
		bw.newLine();
		bw.flush();
		for(CommunicationEventListener l : eventListeners) {
			l.onDataSend(data);
		}
	}
	
	@Override
	public String toString() {
		return "TCPSender[ host : %s || port : %d || connection : %s ]".formatted(this.host, this.port, socket != null && !socket.isClosed() ? "opened" : "closed");
	}
}
