package gkuri.anylib.communication.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import gkuri.anylib.communication.CommunicationData;
import gkuri.anylib.communication.CommunicationEventListener;
import gkuri.anylib.communication.CommunicationPing;
import gkuri.anylib.communication.TwoWayCommunication;
import gkuri.anylib.communication.filter.CommunicationFilter;
import gkuri.anylib.utils.cast.ClassCastUtil;
import gkuri.anylib.utils.multiValue.Duo;

public class TCPConnectionMultiServer implements TwoWayCommunication {
	
	private int port;
	private TCPReceiverHandler receiver;
	private CommunicationFilter filter;
	
	private ServerSocket ss;
	private int timeOut;
	private ExecutorService acceptExecutorService;
	private ExecutorService receiverExecutorService;
	private Consumer<UUID> createConnectionListener;

	private volatile boolean closed;
	
	private final Map<UUID, Connecter> connections = new ConcurrentHashMap<>();
	
	public TCPConnectionMultiServer(int port, TCPReceiverHandler receiver, CommunicationFilter filter, Consumer<UUID> createConnectionListener) {
		this(port, receiver, filter, 60_000, createConnectionListener);
	}
	
	public TCPConnectionMultiServer(int port, TCPReceiverHandler receiver, CommunicationFilter filter, int threadPool, Consumer<UUID> createConnectionListener) {
		this(port, receiver, filter, threadPool, 60_000, createConnectionListener);
	}
	
	public TCPConnectionMultiServer(int port, TCPReceiverHandler receiver, CommunicationFilter filter, int threadPool, int timeOut, Consumer<UUID> createConnectionListener) {
		if(receiver == null || filter == null || createConnectionListener == null) throw new IllegalArgumentException("receiver or filter or create connection listener is null.");
		if(port < 0 || threadPool < 1 || timeOut < 1) throw new IllegalArgumentException("port or thread pool or time out is too few.");
		this.port = port;
		this.receiver = receiver;
		this.filter = filter;
		this.acceptExecutorService = Executors.newSingleThreadExecutor();
		this.receiverExecutorService = Executors.newFixedThreadPool(threadPool);
		this.timeOut = timeOut;
		this.createConnectionListener = createConnectionListener; 
	}
	
	@Override
	public int getTimeOut() {
		return this.timeOut;
	}
	
	public void setTimeOut(int timeOut) {
		if(timeOut < 0) return;
		this.timeOut = timeOut;
	}
	
	@Override
	public boolean open() throws IOException {
		
		this.closed = false;
		this.acceptExecutorService.submit(new AcceptThread());
		return true;
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
	
	@Override
	public boolean isClosed() {
		return this.closed;
	}
	
	public Connecter getConnection(UUID id) {
		return connections.get(id);
	}
	
	@Override
	public void send(CommunicationData<?> data) throws JsonProcessingException, IOException {
		for(UUID id : connections.keySet()) {
			try {
				connections.get(id).send(data);
			} catch (Exception ignore) {}
		}
	}

	@Deprecated
	@Override
	public long sendPingMS() throws UnknownHostException, IOException, InterruptedException {
		throw new UnsupportedOperationException();
	}
	
	private class AcceptThread implements Runnable {
		
		@Override
		public void run() {
			try {
				ss = new ServerSocket(port);
				while (!closed) {
					UUID threadId = UUID.randomUUID();
					CountDownLatch latch = new CountDownLatch(1);
					Connecter thread = new Connecter(threadId, ss.accept(), filter, receiver, new LinkedList<CommunicationEventListener>(), latch);
					connections.put(threadId, thread);
					receiverExecutorService.submit(thread);
					if(latch.await(10_000, TimeUnit.MILLISECONDS)) {
						createConnectionListener.accept(threadId);
					}
				}
			} catch (Exception e) {
				close();
				if(!closed) e.printStackTrace();
			}
		}
		
	}
	
	public class Connecter extends TCPTwoWayReceiverThread {

		private UUID id;
		private Socket socket;
		private volatile CommunicationFilter filter;
		private volatile TCPReceiverHandler receiver;
		
		private BufferedReader br;
		private BufferedWriter bw;
		
		private boolean threadClosed = true;
		
		private final Map<UUID, Duo<CountDownLatch, CommunicationPing>> pingMap = new ConcurrentHashMap<>();
		private LinkedList<CommunicationEventListener> eventListeners;
		
		private final CountDownLatch latch;
		
		private Connecter(UUID id, Socket socket, CommunicationFilter filter, TCPReceiverHandler receiver, LinkedList<CommunicationEventListener> eventListeners, CountDownLatch latch) {
			super(TCPConnectionMultiServer.this, socket, filter, receiver, eventListeners);
			this.id = id;
			this.socket = socket;
			this.filter = filter;
			this.receiver = receiver;
			this.eventListeners = eventListeners;
			this.latch = latch;
		}
		
		@Override
		public void run() {
			if(!threadClosed) return;
			threadClosed = false;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
				br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				this.socket.setSoTimeout(timeOut);
				for(CommunicationEventListener l : eventListeners) {
					l.onConnectionOpen();
				}
				
				Executor executor = Executors.newSingleThreadExecutor();
				latch.countDown();
				while (!closed && !threadClosed) {
					String receiveText = br.readLine();
					if(closed || threadClosed) break;
					if(receiveText == null) break;
					executor.execute(() -> {
						try {
							JsonNode root = CommunicationData.mapper.readTree(receiveText);
		//					System.out.println(root);
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
							
							CommunicationFilter filter = this.filter;
							TCPReceiverHandler receiver = this.receiver;
							
							if(filter != null && !filter.isAllowed(data)) {
								for(CommunicationEventListener l : eventListeners) {
									l.onReceiveDataBlocked(root);
								}
								return;
							}
							
							receiver.receive(data);
							for(CommunicationEventListener l : eventListeners) {
								l.onDataReceived(data);
							}
							
						} catch(ClassNotFoundException ignore) {
							return;
						} catch(SocketTimeoutException socketTimeOut) {
							this.receiver.socketTimeOut(socketTimeOut);
							return;
						} catch(SocketException socketE) {
							if(closed) return;
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
		
		public synchronized void send(CommunicationData<?> data) throws UnknownHostException, IOException {
			if(this.isClosed()) return;
			String json = CommunicationData.mapper.writeValueAsString(data);
			bw.write(json);
			bw.newLine();
			bw.flush();
			for(CommunicationEventListener l : eventListeners) {
				l.onDataSend(data);
			}
		}
		
		@Override
		public void close() {
			threadClosed = true;
			connections.remove(this.id);
			if(br != null)
				try {
					br.close();
				} catch (IOException ignore) {}
			if(socket != null)
				try {
					socket.close();
				} catch (IOException ignore) {}
			for(CommunicationEventListener l : eventListeners) {
				l.onConnectionClose();
			}
		}

		@Override
		public void receivePing(CommunicationData<?> pingData) throws IOException {
			CommunicationPing ping = ClassCastUtil.cast(pingData.getData(), CommunicationPing.class);
			Duo<CountDownLatch, CommunicationPing> pingDuo = pingMap.get(ping.getId());
			if(pingDuo == null) {
				send(pingData);
				return;
			}
			pingDuo.getFirst().countDown();
		}
		
		public long sendPingMS() throws UnknownHostException, IOException, InterruptedException {
			CommunicationPing cp = null;
			try {
				open();
				CommunicationData<CommunicationPing> pingData = new CommunicationData<>();
				CountDownLatch latch = new CountDownLatch(1);
				cp = new CommunicationPing();
				pingData.setData(cp);
				this.pingMap.put(cp.getId(), new Duo<>(latch, cp));
				send(pingData);
				if(latch.await(10_000, TimeUnit.MILLISECONDS)) {
					long responseReceive = System.currentTimeMillis();
					this.pingMap.remove(cp.getId());
					return responseReceive - cp.getTimeMillis();
				} else {
					this.pingMap.remove(cp.getId());
					return -1;
				}
			} catch (UnknownHostException e) {
				if(cp != null) this.pingMap.remove(cp.getId());
				throw e;
			} catch (IOException e) {
				if(cp != null) this.pingMap.remove(cp.getId());
				throw e;
			} catch (InterruptedException e) {
				if(cp != null) this.pingMap.remove(cp.getId());
				throw e;
			}
		}
		
		public UUID getId() {
			return this.id;
		}
		
		public void swapReceiver(TCPReceiverHandler receiver) {
			if(receiver == null) return;
			this.receiver = receiver;
		}
		
		public void swapFilter(CommunicationFilter filter) {
			if(filter == null) return;
			this.filter = filter;
		}
		
		public boolean isClosed() {
			return closed || threadClosed;
		}
	}
}
