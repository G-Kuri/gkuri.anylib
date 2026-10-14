package gkuri.anylib.communication;

import com.fasterxml.jackson.databind.JsonNode;

public interface CommunicationEventListener {
	
	default public void onConnectionOpen() {
		
	}
	
	default public void onConnectionClose() {
		
	}

	default public void onDataSend(CommunicationData<?> data) {
		
	}
	
	default public void onDataReceived(CommunicationData<?> data) {
		
	}
	
	default public void onPingReceived(CommunicationData<?> data) {
		
	}

	default public void onReceiveDataBlocked(JsonNode root) {
		
	}
	
}
