package gkuri.anylib.communication.tcp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.JsonNode;

import gkuri.anylib.communication.CommunicationData;
import gkuri.anylib.communication.CommunicationEventListener;
import gkuri.anylib.communication.filter.CommunicationFilter;

public class TCPReceiver implements Closeable {
	
	private int port;
	private TCPReceiverHandler receiver;
	private CommunicationFilter filter;
	
	private ServerSocket ss;
	private int timeOut;
	private ExecutorService acceptExecutorService;
	private ExecutorService receiverExecutorService;

	private volatile boolean closed;
	
	private LinkedList<CommunicationEventListener> eventListeners = new LinkedList<>();

	public TCPReceiver(int port, TCPReceiverHandler receiver, CommunicationFilter filter) {
		this(port, receiver, filter, 60_000);
	}
	
	public TCPReceiver(int port, TCPReceiverHandler receiver, CommunicationFilter filter, int threadPool) {
		this(port, receiver, filter, threadPool, 60_000);
	}
	
	public TCPReceiver(int port, TCPReceiverHandler receiver, CommunicationFilter filter, int threadPool, int timeOut) {
		if(receiver == null || filter == null) throw new IllegalArgumentException("receiver or filter is null.");
		if(port < 0 || threadPool < 1 || timeOut < 1) throw new IllegalArgumentException("port or thread pool or time out is too few.");
		this.port = port;
		this.receiver = receiver;
		this.filter = filter;
		this.acceptExecutorService = Executors.newSingleThreadExecutor();
		this.receiverExecutorService = Executors.newFixedThreadPool(threadPool);
		this.timeOut = timeOut;
	}
	
	public int getTimeOut() {
		return this.timeOut;
	}
	
	public void setTimeOut(int timeOut) {
		if(timeOut < 0) return;
		this.timeOut = timeOut;
	}
	
	public void open() throws IOException {
		
		this.closed = false;

		this.acceptExecutorService.submit(new AcceptThread());
	}
	
	@Override
	public void close() {
		this.closed = true;
		this.acceptExecutorService.shutdown();
		this.receiverExecutorService.shutdown();
		try {
	        if (ss != null) ss.close();
	    } catch (IOException ignored) {}
	    ss = null;
	}
	
	public boolean isClosed() {
		return this.closed;
	}
	
	public void addEventListener(CommunicationEventListener listener) {
		if(listener == null) return;
		this.eventListeners.add(listener);
	}
	
	public boolean removeEventListener(CommunicationEventListener listener) {
		return this.eventListeners.remove(listener);
	}
	
	private class AcceptThread implements Runnable {
		
		@Override
		public void run() {
			try {
				ss = new ServerSocket(port);
				while (!closed) {
					Receiver thread = new Receiver(ss.accept(), filter, receiver, eventListeners);
					receiverExecutorService.submit(thread);
				}
				
			} catch (Exception e) {
				close();
				if(!closed) e.printStackTrace();
			}
		}
		
	}
	
	public class Receiver extends TCPTwoWayReceiverThread {

		private Socket socket;
		private CommunicationFilter filter;
		private TCPReceiverHandler receiver;
		
		private BufferedReader br;
		
		private boolean threadClosed = true;
		
		private Receiver(Socket socket, CommunicationFilter filter, TCPReceiverHandler receiver, LinkedList<CommunicationEventListener> eventListeners) {
			super(null, socket, filter, receiver, eventListeners);
			this.socket = socket;
			this.filter = filter;
			this.receiver = receiver;
		}
		
		@Override
		public void run() {
			if(!threadClosed) return;
			threadClosed = false;
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				this.socket.setSoTimeout(timeOut);
				
				while (!closed && !threadClosed) {
					try {
						String receiveText = br.readLine();
						if(closed || threadClosed) break;
						if(receiveText == null) break;
						JsonNode root = CommunicationData.mapper.readTree(receiveText);
	//					System.out.println(root);
						JsonNode classNode = root.get("dataClass");
						if (classNode == null || classNode.isNull()) continue;
						String clazzName = classNode.asText();
					
						if(!filter.isAllowedDataTypeByClassName(clazzName)) continue;

						CommunicationData<?> data = CommunicationData.parseJson(root, clazzName);
						
						if(data == null) continue;
						if(this.filter != null && !this.filter.isAllowed(data)) continue;
						
						this.receiver.receive(data);
						
					} catch(ClassNotFoundException ignore) {
						continue;
					} catch(SocketTimeoutException socketTimeOut) {
						this.receiver.socketTimeOut(socketTimeOut);
						break;
					}catch(Exception e) {
						this.receiver.catchException(this, e);
					}
				}
				close();
			}catch (Exception e) {
				if(br != null)
					try {
						br.close();
					} catch (IOException ignore) {}
				if(socket != null)
					try {
						socket.close();
					} catch (IOException ignore) {}
				e.printStackTrace();
			}
		}
		
		@Override
		public void close() {
			threadClosed = true;
			if(br != null)
				try {
					br.close();
				} catch (IOException ignore) {}
			if(socket != null)
				try {
					socket.close();
				} catch (IOException ignore) {}
		}

		@Override
		public void receivePing(CommunicationData<?> pingData) throws IOException {
			throw new UnsupportedOperationException();
		}
	}
}
