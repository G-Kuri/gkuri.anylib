package gkuri.anylib.communication.tcp;

import java.net.SocketTimeoutException;

import gkuri.anylib.communication.CommunicationData;

@FunctionalInterface
public interface TCPReceiverHandler{
	
	public void receive(CommunicationData<?> data);
	
	public default void catchException(TCPReceiveThread receiverThread, Exception e) {
		e.printStackTrace();
		receiverThread.close();
	}
	
	public default void socketTimeOut(SocketTimeoutException socketTimeOut) {
		
	}
	
}
