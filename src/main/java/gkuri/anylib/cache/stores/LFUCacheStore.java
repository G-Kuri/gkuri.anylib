package gkuri.anylib.cache.stores;

import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.cache.CacheData;
import gkuri.anylib.cache.CacheStore;
import gkuri.anylib.cache.events.CacheEventListener;

/**
 * LFU方式でキャッシュをクリンナップします。
 * CacheStoreの子クラスです。
 * @param <T>
 */
public class LFUCacheStore<T> extends CacheStore<T> {

	private int capacity;
	
	public LFUCacheStore(int capacity) {
		super(capacity);
		this.capacity = capacity;
	}

	@Override
	protected void putData(CacheData<T> data) {
		if(this.cacheDatas.get(data.getKey()) != null) return;
		
		if(this.capacity <= this.cacheDatas.size()) {
			List<CacheData<?>> cleanupTargets = new ArrayList<>();
			List<CacheData<T>> list = this.getSortedCacheDatasByCallsCount();
			while (this.capacity <= list.size()) {
				CacheData<T> target = list.removeFirst();
				cleanupTargets.add(target);
				this.remove(target.getKey());
			}
			
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onCleanup(cleanupTargets);
			}
		}
	}
}
