package gkuri.anylib.cache.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import gkuri.anylib.cache.CacheData;
import gkuri.anylib.cache.CacheStore;
import gkuri.anylib.cache.events.CacheEventListener;

/**
 * TTL方式でキャッシュをクリンナップします。
 * CacheStoreの子クラスです。
 * @param <T>
 */
public class TTLCacheStore<T> extends CacheStore<T> {

	private int capacity;
	private long lifeTime;
	
	public TTLCacheStore(int capacity, int lifeTime, TimeUnit timeUnit) {
		super(capacity);
		this.capacity = capacity;
		this.lifeTime = TimeUnit.MILLISECONDS.convert(lifeTime, timeUnit);;
	}

	@Override
	protected void putData(CacheData<T> data) {
		List<CacheData<?>> cleanupTargets = new ArrayList<>();
		List<CacheData<T>> list = this.getSortedCacheDatasByCallsCurrentTimeMillis();
		if(this.capacity <= this.cacheDatas.size()) {
			while (this.capacity <= list.size()) {
				cleanupTargets.add(list.removeFirst());
				this.remove(list.removeFirst().getKey());
			}
		}
		
		long target = System.currentTimeMillis() - lifeTime;
		for(CacheData<T> cacheData : list) {
			if(cacheData.getCallsCurrentTimeMillis() < target) {
				cleanupTargets.add(cacheData);
				this.cacheDatas.remove(cacheData.getKey());
				continue;
			}
			break;
		}
		
		if(!cleanupTargets.isEmpty()) {
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onCleanup(cleanupTargets);
			}
		}
	}
	
	@Override
	protected boolean getData(CacheData<T> data) {
		long target;
		List<CacheData<?>> cleanupTargets = new ArrayList<>();
		if((target = System.currentTimeMillis() - lifeTime) <= data.getCallsCurrentTimeMillis()) {
			List<CacheData<T>> list = this.getSortedCacheDatasByCallsCurrentTimeMillis();
			for(CacheData<T> cacheData : list) {
				if(cacheData.getCallsCurrentTimeMillis() < target) {
					cleanupTargets.add(cacheData);
					this.cacheDatas.remove(cacheData.getKey());
					continue;
				}
				
				for(CacheEventListener listener : cacheEventListeners) {
					listener.onCleanup(cleanupTargets);
				}
				return false;
			}
		}
		return true;
	}
}
