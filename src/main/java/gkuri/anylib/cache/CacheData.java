package gkuri.anylib.cache;

public class CacheData<T> {
	
	private long callsCount;
	private long callsCurrentTimeMillis;
	private long createCurrentTimeMillis;
	private String key;
	private T data;
	
	protected CacheData(String key, T data) {
		this.callsCount = 0;
		this.callsCurrentTimeMillis = -1;
		this.createCurrentTimeMillis = System.currentTimeMillis();
		this.key = key;
		this.data = data;
	}
	
	/**
	 * 現在までに何回呼び出されたかを返します。
	 * @return
	 */
	public long getCallsCount() {
		return this.callsCount;
	}
	
	protected void setCallsCount(long callsCount) {
		this.callsCount = callsCount;
	}
	
	protected void incCallsCount() {
		this.callsCount++;
	}
	
	/**
	 * 最後に呼び出された時間を返します。
	 * @return
	 */
	public long getCallsCurrentTimeMillis() {
		return this.callsCurrentTimeMillis;
	}
	
	protected void setCallsCurrentTimeMillis(long callsCurrentTimeMillis) {
		this.callsCurrentTimeMillis = callsCurrentTimeMillis;
	}
	
	protected void setCallsCurrentTimeMillisNow() {
		this.callsCurrentTimeMillis = System.currentTimeMillis();
	}
	
	/**
	 * キャッシュデータが保存又は更新された時間を返します。
	 * @return
	 */
	public long getCreateCurrentTimeMillis() {
		return this.createCurrentTimeMillis;
	}
	
	protected void setCreateCurrentTimeMillis(long createCurrentTimeMillis) {
		this.createCurrentTimeMillis = createCurrentTimeMillis;
	}
	
	/**
	 * このキャッシュデータに紐づくキーを返します。
	 * @return
	 */
	public String getKey() {
		return this.key;
	}
	
	protected void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * キャッシュの実データを返します。
	 * @return
	 */
	public T getData() {
		return this.data;
	}
	
	protected void setData(T data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		return "CacheData[ %s : %s || count : %d || call time : %d || crate time : %d ]".formatted(key, data, callsCount, callsCurrentTimeMillis, createCurrentTimeMillis);
	}
}
