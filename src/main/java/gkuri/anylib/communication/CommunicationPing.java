package gkuri.anylib.communication;

import java.util.UUID;

public class CommunicationPing implements CommunicationNoLockData {
	
	private long timeMillis;
	private UUID id;

	public CommunicationPing() {
		this.timeMillis = System.currentTimeMillis();
		this.id = UUID.randomUUID();
	}
	
	public long getTimeMillis() {
		return timeMillis;
	}
	
	public void setTimeMillis(long timeMillis) {
		this.timeMillis = timeMillis;
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "CommunicationPing[ id:%s || create time:%s ]".formatted(this.id, this.timeMillis);
	}
}
