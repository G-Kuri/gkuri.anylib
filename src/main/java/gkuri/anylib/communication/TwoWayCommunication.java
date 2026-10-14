package gkuri.anylib.communication;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface TwoWayCommunication extends Closeable {

	public boolean open() throws UnknownHostException, IOException;
	
	public void send(CommunicationData<?> data) throws JsonProcessingException, IOException;
	
	public long sendPingMS() throws UnknownHostException, IOException, InterruptedException;
	
	public int getTimeOut();
	
	public boolean isClosed();
}
