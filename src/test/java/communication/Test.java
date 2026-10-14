package communication;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import gkuri.anylib.communication.CommunicationData;
import gkuri.anylib.communication.CommunicationEventListener;
import gkuri.anylib.communication.filter.CommunicationBlackList;
import gkuri.anylib.communication.tcp.TCPConnectionClient;
import gkuri.anylib.communication.tcp.TCPConnectionMultiServer;
import gkuri.anylib.communication.tcp.TCPConnectionMultiServer.Connecter;
import gkuri.anylib.communication.tcp.TCPConnectionServer;
import gkuri.anylib.communication.tcp.TCPReceiver;
import gkuri.anylib.communication.tcp.TCPSender;
import gkuri.anylib.logs.Log;

public class Test {
	
	static TCPConnectionServer cs;
	static TCPConnectionClient cc;
	static TCPConnectionClient cc2;
	static TCPConnectionMultiServer cms;
	static Map<UUID, Connecter> connecterMap = new HashMap<>();

	public static void main(String[] args) {
		
		TCPSender sender1 = new TCPSender("localhost", 25565);
		TCPSender sender2 = new TCPSender("localhost", 25565);
		TCPReceiver receiver = new TCPReceiver(25565, (data) -> {
			System.out.println(data);
		}, new CommunicationBlackList());
		System.out.println("one way ok");
		cs = new TCPConnectionServer(25566, (data) -> {
			System.out.println("client say : " + data.getData());
		}, new CommunicationBlackList().addBlockDataType(Log.class));
		System.out.println("twe way server ok");
		cc = new TCPConnectionClient("localhost", 25566, (data) -> {
			System.out.println("server say : " + data.getData());
		}, new CommunicationBlackList());
		cc2 = new TCPConnectionClient("localhost", 25566, (data) -> {
			System.out.println("2 server say : " + data.getData());
		}, new CommunicationBlackList());
		System.out.println("twe way client ok");
		cms = new TCPConnectionMultiServer(25566, (data) -> {
			System.out.println("client(m) say : " + data.getData());
		}, new CommunicationBlackList(), (id) -> {
			addConnecton(id);
		});
		
		try {
			receiver.open();
			System.out.println("receiver open");
			sender1.open();
			System.out.println("sender1 open");
			sender2.open();
			System.out.println("sender2 open");
			new Thread(() -> {
				try {
					cs.open();
				} catch (UnknownHostException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				System.out.println("cs open");
			}).start();
			
			Thread.sleep(100);
			cc.open();
			System.out.println("cc open");
			
			EventCounter cce = new EventCounter();
			EventCounter cse = new EventCounter();
			
			cc.addEventListener(cce);
			cs.addEventListener(cse);
			
			CommunicationData<String> stringData = new CommunicationData<>();
			CommunicationData<Integer> intData = new CommunicationData<>();
			CommunicationData<Character> charData = new CommunicationData<>();
			CommunicationData<Log> logData = new CommunicationData<>();
			CommunicationData<Boolean> boolData = new CommunicationData<>();
			CommunicationData<Long> longData = new CommunicationData<>();
			
			stringData.setData("string data");
			System.out.println("send data 1");
			sender1.send(stringData);
			
			intData.setData(123);
			System.out.println("send data 2");
			sender2.send(intData);
			
			charData.setData('c');
			System.out.println("send data 3");
			sender1.send(charData);
			
			logData.setData(new Log("log title", "log message"));
			System.out.println("send data 4");
			sender2.send(logData);
			
			longData.setData(10000L);
			System.out.println("send data 5");
			cc.send(longData);
			
			boolData.setData(true);
			System.out.println("send data 6");
			cs.send(boolData);
			
			System.out.println("send data 7");
			cc.send(logData);
			
			for (int i = 0; i < 5; i++) {
				System.out.println("send ping(" + i + ") s2c : " + cs.sendPingMS() + "ms");
				System.out.println("send ping(" + i + ") c2s : " + cc.sendPingMS() + "ms");
			}
			
			Thread.sleep(1000);
			receiver.close();
			sender1.close();
			sender2.close();
			cc.close();
			cs.close();
			
			cms.open();
			
			cc.open();
			cc2.open();
			
			Thread.sleep(50); //cc2の接続確立待ち
			stringData.setData("bloadcast");
			cms.send(stringData);
			
			cc.send(intData);
			cc.send(boolData);
			cc2.send(longData);
			
			Thread.sleep(50);
			for(UUID id : connecterMap.keySet()) {
				System.out.print(id + " event -> ");
				EventCounter c =(EventCounter) connecterMap.get(id).getEventListeners().get(0);
				c.printCount();
			}
			cc.close();
			cc2.close();
			
			cc.open();
			Thread.sleep(50);
			stringData.setData("reconnection");
			cc.send(stringData);
			
			Thread.sleep(50);
			for(UUID id : connecterMap.keySet()) {
				System.out.print(id + " event -> ");
				EventCounter c =(EventCounter) connecterMap.get(id).getEventListeners().get(0);
				c.printCount();
			}
			cc.close();
			cms.close();
			
			System.out.println("reconnection to closed server : " + cc.open());
			
			System.out.print("cse event -> ");
			cse.printCount();
			System.out.print("cce event -> ");
			cce.printCount();
			
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	static int count = 0;
	private static void addConnecton(UUID id) {
		Connecter connecter = cms.getConnection(id);
		connecter.addEventListener(new EventCounter(connecter));
		switch (count++) {
			case 0:
				connecter.swapFilter(new CommunicationBlackList().addBlockDataType(Integer.class));
				break;
			case 1:
				connecter.swapReceiver(data -> {
					System.out.println("client(swaped) say : " + data.getData());
				});
				break;
		}
		connecterMap.put(id, connecter);
		System.out.println("connect " + id);
	}
	
	public static class EventCounter implements CommunicationEventListener {
		
		private int send = 0;
		private int receive = 0;
		private int block = 0;
		private int ping = 0;
		
		Connecter connecter = null;
		
		public EventCounter() {
			
		}
		
		public EventCounter(Connecter connecter) {
			this.connecter = connecter;
		}
		
		@Override
		public void onConnectionOpen() {
		}
		
		@Override
		public void onConnectionClose() {
			if(this.connecter != null) connecterMap.remove(connecter.getId());
		}
		
		@Override
		public void onDataSend(CommunicationData<?> data) {
			send++;
		}
		
		@Override
		public void onDataReceived(CommunicationData<?> data) {
			receive++;
		}

		@Override
		public void onReceiveDataBlocked(JsonNode root) {
			block++;
		}
		
		@Override
		public void onPingReceived(CommunicationData<?> data) {
			ping++;
		}
		
		public void printCount() {
			System.out.println("send:%d - receive:%d - block:%d - ping:%d".formatted(send, receive, block, ping));
		}
		
	}
}
