package gkuri.anylib.cache.stores;

import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.cache.CacheData;
import gkuri.anylib.cache.CacheStore;
import gkuri.anylib.cache.events.CacheEventListener;

/**
 * LRU方式でキャッシュをクリンナップします。
 * CacheStoreの子クラスです。
 * @param <T>
 */
public class LRUCacheStore<T> extends CacheStore<T> {

	private int capacity;
	
	public LRUCacheStore(int capacity) {
		super(capacity);
		this.capacity = capacity;
	}

	@Override
	protected void putData(CacheData<T> data) {
		if(this.capacity <= this.cacheDatas.size()) {
			List<CacheData<?>> cleanupTargets = new ArrayList<>();
			List<CacheData<T>> list = this.getSortedCacheDatasByCallsCurrentTimeMillis();
			while (this.capacity <= list.size()) {
				cleanupTargets.add(list.removeFirst());
				this.remove(list.removeFirst().getKey());
			}
			
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onCleanup(cleanupTargets);
			}
		}
	}

}
