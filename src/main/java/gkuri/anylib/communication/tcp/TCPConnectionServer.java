package gkuri.anylib.communication.tcp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import gkuri.anylib.communication.CommunicationData;
import gkuri.anylib.communication.CommunicationEventListener;
import gkuri.anylib.communication.CommunicationPing;
import gkuri.anylib.communication.TwoWayCommunication;
import gkuri.anylib.communication.filter.CommunicationFilter;
import gkuri.anylib.utils.cast.ClassCastUtil;
import gkuri.anylib.utils.multiValue.Duo;

public class TCPConnectionServer implements TwoWayCommunication {

	private int port;
	private TCPReceiverHandler receiver;
	private CommunicationFilter filter;
	private int timeOut;
	
	private Socket socket;
	private ServerSocket serverSocket;
	private BufferedWriter bw;
	
	protected volatile boolean closed;
	
	private final Map<UUID, Duo<CountDownLatch, CommunicationPing>> pingMap = new ConcurrentHashMap<>();
	private LinkedList<CommunicationEventListener> eventListeners = new LinkedList<>();
	
	public TCPConnectionServer(int port, TCPReceiverHandler receiver, CommunicationFilter filter) {
		this( port, receiver, filter, 60_000);
	}
	
	public TCPConnectionServer(int port, TCPReceiverHandler receiver, CommunicationFilter filter, int timeOut) {
		if(receiver == null || filter == null) throw new IllegalArgumentException("receiver or filter is null.");
		if(port < 0 || timeOut < 1) throw new IllegalArgumentException("port or time out is too few.");
		this.port = port;
		this.receiver = receiver;
		this.filter = filter;
		this.timeOut = timeOut;
	}
	
	@Override
	public synchronized boolean open() throws UnknownHostException, IOException {
		this.closed = !openProsess();
		return !this.closed;
	}
	
	private boolean openProsess() throws UnknownHostException, IOException  {
		if(this.socket != null && !this.socket.isClosed()) return true;

		this.serverSocket = new ServerSocket(this.port);
		this.serverSocket.setSoTimeout(this.timeOut);
		try {
			this.socket = this.serverSocket.accept();
			this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
			Thread receiveThread = new Thread(new Receiver(this.socket, filter, receiver, eventListeners));
			receiveThread.start();
			this.serverSocket.close();
			return true;
		} catch (SocketTimeoutException socketTimeOut) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public synchronized void send(CommunicationData<?> data) throws IOException {
		String json = CommunicationData.mapper.writeValueAsString(data);
		bw.write(json);
		bw.newLine();
		bw.flush();
		for(CommunicationEventListener l : eventListeners) {
			l.onDataSend(data);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if(bw != null) bw.close();
		} catch(IOException ignore) {}
		try {
			if(socket != null) socket.close();
		} catch(IOException ignore) {}
		try {
			if(serverSocket != null) serverSocket.close();
		} catch(IOException ignore) {}
		
		bw = null;
		socket = null;
		serverSocket = null;
		this.closed = true;
	}
	
	@Override
	public int getTimeOut() {
		return this.timeOut;
	}
	
	@Override
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
	
	/**
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Override
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
	
	public class Receiver extends TCPTwoWayReceiverThread {

		public Receiver(Socket socket, CommunicationFilter filter, TCPReceiverHandler receiver, LinkedList<CommunicationEventListener> eventListeners) {
			super(TCPConnectionServer.this, socket, filter, receiver, eventListeners);
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
	}
}
