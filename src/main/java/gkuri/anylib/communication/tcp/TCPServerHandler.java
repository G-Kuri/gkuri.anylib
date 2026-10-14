package gkuri.anylib.communication.tcp;

import gkuri.anylib.communication.CommunicationData;

public interface TCPServerHandler extends TCPReceiverHandler {
	
	public void send(CommunicationData<?> data);

}
