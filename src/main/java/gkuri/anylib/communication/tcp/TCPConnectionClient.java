package gkuri.anylib.communication.tcp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
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

public class TCPConnectionClient implements TwoWayCommunication {

	private String host;
	private int port;
	private TCPReceiverHandler receiver;
	private CommunicationFilter filter;
	private int timeOut;
	
	private Socket socket;
	private ServerSocket serverSocket;
	private BufferedWriter bw;
	
	private volatile boolean closed;
		
	private final Map<UUID, Duo<CountDownLatch, CommunicationPing>> pingMap = new ConcurrentHashMap<>();
	private LinkedList<CommunicationEventListener> eventListeners = new LinkedList<>();

	public TCPConnectionClient(String host, int port, TCPReceiverHandler receiver, CommunicationFilter filter) {
		this(host, port, receiver, filter, 60_000);
	}
	
	public TCPConnectionClient(String host, int port, TCPReceiverHandler receiver, CommunicationFilter filter, int timeOut) {
		if(host == null || receiver == null || filter == null) throw new IllegalArgumentException("host or receiver or filter is null.");
		if(port < 0 || timeOut < 1) throw new IllegalArgumentException("port or time out is too few.");
		this.host = host;
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
		Socket socket = null;
		BufferedWriter bw = null;
		
		int retry = 0;
		Random random = new Random();
		
		while (retry < 5) {
			try {
				Thread.sleep(random.nextInt(2000));
			} catch (InterruptedException ignore) {
			}
			try {
				socket = new Socket(this.host, this.port);
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
				
				this.socket = socket;
				this.bw = bw;
				Thread receiveThread = new Thread(new Receiver(this.socket, filter, receiver, eventListeners));
				receiveThread.start();
				return true;
			} catch (Exception e) {
				retry++;
				if(socket != null) socket.close();
				if(bw != null) bw.close();
				continue;
			}
		}
		return false;
	}
	
	@Override
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
			super(TCPConnectionClient.this, socket, filter, receiver, eventListeners);
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
