package gkuri.anylib.communication.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.JsonNode;

import gkuri.anylib.communication.CommunicationData;
import gkuri.anylib.communication.CommunicationEventListener;
import gkuri.anylib.communication.CommunicationPing;
import gkuri.anylib.communication.TwoWayCommunication;
import gkuri.anylib.communication.filter.CommunicationFilter;

abstract class TCPTwoWayReceiverThread implements TCPReceiveThread {

	private TwoWayCommunication com;
	private Socket socket;
	private CommunicationFilter filter;
	private TCPReceiverHandler receiver;
	private List<CommunicationEventListener> eventListeners;
	
	private BufferedReader br;
	
	private volatile boolean threadClosed = true;
	
	protected TCPTwoWayReceiverThread(TwoWayCommunication com, Socket socket, CommunicationFilter filter, TCPReceiverHandler receiver, LinkedList<CommunicationEventListener> eventListeners) {
		this.com = com;
		this.socket = socket;
		this.filter = filter;
		this.receiver = receiver;
		this.eventListeners = eventListeners;
	}
	
	@Override
	public void run() {
		if(!threadClosed) return;
		threadClosed = false;
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			this.socket.setSoTimeout(com.getTimeOut());
			Executor executor = Executors.newSingleThreadExecutor();
			
			while (!com.isClosed() && !threadClosed) {
				String receiveText = br.readLine();
				if(com.isClosed() || threadClosed) break;
				if(receiveText == null) break;
				JsonNode root = CommunicationData.mapper.readTree(receiveText);
				executor.execute(() -> {
					try {
//						System.out.println(root);
						JsonNode classNode = root.get("dataClass");
						if (classNode == null || classNode.isNull()) return;
						String clazzName = classNode.asText();
					
						boolean isPing = CommunicationPing.class.getName().equals(clazzName);
						
						if(!isPing && !filter.isAllowedDataTypeByClassName(clazzName)) {
							for(CommunicationEventListener l : eventListeners) {
								l.onReceiveDataBlocked(root);
							}
							return;
						}
						CommunicationData<?> data = CommunicationData.parseJson(root, clazzName);
						if(data == null) return;
						
						if(isPing) {
							receivePing(data);
							for(CommunicationEventListener l : eventListeners) {
								l.onPingReceived(data);
							}
							return;
						}
						
						if(this.filter != null && !this.filter.isAllowed(data)) {
							for(CommunicationEventListener l : eventListeners) {
								l.onReceiveDataBlocked(root);
							}
							return;
						}
						
						this.receiver.receive(data);
						for(CommunicationEventListener l : eventListeners) {
							l.onDataReceived(data);
						}
						
					} catch(ClassNotFoundException ignore) {
						return;
					} catch(SocketTimeoutException socketTimeOut) {
						this.receiver.socketTimeOut(socketTimeOut);
						return;
					} catch(SocketException socketE) {
						if(com.isClosed()) return;
						this.receiver.catchException(this, socketE);
					} catch(Exception e) {
						this.receiver.catchException(this, e);
					}
				});
			}
			close();
		}catch (Exception e) {
			close();
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		try {
			this.com.close();
		}catch (IOException ignore) {
		}
		this.threadClosed = true;
		for(CommunicationEventListener l : eventListeners) {
			l.onConnectionClose();
		}
	}
	
	public void addEventListener(CommunicationEventListener listener) {
		if(listener == null) return;
		this.eventListeners.add(listener);
	}
	
	public List<CommunicationEventListener> getEventListeners(){
		return List.copyOf(this.eventListeners);
	}
	
	public boolean removeEventListener(CommunicationEventListener listener) {
		return this.eventListeners.remove(listener);
	}
	
	public abstract void receivePing(CommunicationData<?> data) throws IOException;
}
