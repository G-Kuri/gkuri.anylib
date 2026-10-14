package gkuri.anylib.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import gkuri.anylib.cache.events.CacheEventListener;

/**
 * キャッシュストアの基底クラスです。
 * キャッシュ系のクラスを作成する際にextendsすれば各種基本メソッドを使用できます。
 * また、子クラスはputDataをOverrideしデータ追加時にデータのクリンナップを行ってください。
 * @param <T>
 */
public abstract class CacheStore<T> {

	protected Map<String, CacheData<T>> cacheDatas;
	protected List<CacheEventListener> cacheEventListeners;
	
	protected CacheStore(int cacheCapa) {
		if(0 <= cacheCapa) {
			this.cacheDatas = new ConcurrentHashMap<>(cacheCapa);
		} else {
			this.cacheDatas = new ConcurrentHashMap<>();
		}
		this.cacheEventListeners = new CopyOnWriteArrayList<>();
	}
	
	public synchronized void put(String key, T data) {
		CacheData<T> cacheData;
		if((cacheData = this.cacheDatas.get(key)) != null) {
			cacheData.setData(data);
			cacheData.setCreateCurrentTimeMillis(System.currentTimeMillis());
		} else {
			cacheData = new CacheData<T>(key, data);
		}
		
		this.putData(cacheData);
		
		cacheData.incCallsCount();
		cacheData.setCallsCurrentTimeMillisNow();
		
		this.cacheDatas.put(key, cacheData);
		
		for(CacheEventListener listener : cacheEventListeners) {
			listener.onPut(cacheData);
		}
	}
	
	public synchronized void remove(String key) {
		CacheData<T> cacheData = this.cacheDatas.remove(key);
		if(cacheData != null) {
			this.removeData(cacheData);
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onRemove(cacheData);
			}
		}
	} 
	
	public synchronized void clear() {
		this.clearData();
		this.cacheDatas.clear();
		for(CacheEventListener listener : cacheEventListeners) {
			listener.onClear();
		}
	}
	
	public synchronized T get(String key){
		CacheData<T> data = this.cacheDatas.get(key);
		if(data == null) return null;
		
		data.incCallsCount();
		data.setCallsCurrentTimeMillisNow();
		if(this.getData(data)) {
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onGet(data);
			}
			return data.getData();
		}
		return null;
	}
	
	public Set<String> keySet(){
		return this.cacheDatas.keySet();
	}
	
	/**
	 * putした際に呼ばれるので、必要に応じてcacheDatasから削除する処理を書く。
	 * 呼び出しはデータをcacheDatasに保存する直前。
	 * @param data
	 */
	abstract protected void putData(CacheData<T> data);
	
	/**
	 * getした際に呼ばれるので、何かしたければどうぞ。
	 * 呼び出し回数と最終呼出時間の更新はCacheStoreで行うため不要。
	 * falseを返すとgetの戻り値をNULLに変えられる。
	 * @param data
	 */
	protected boolean getData(CacheData<T> data) {
		return true;
	}
	
	/**
	 * removeした際に呼ばれるので、何かしたければどうぞ。
	 * 呼び出しはデータをcacheDatasから削除する直後。
	 * @param data
	 */
	protected void removeData(CacheData<T> data) {
		
	}
	
	/**
	 * clearした際に呼ばれるので、何かしたければどうぞ。
	 * 呼び出しはcacheDatasをクリアする直前。
	 * @param data
	 */
	protected void clearData() {
		
	}
	
	protected synchronized List<CacheData<T>> getCacheDatas() {
		return new ArrayList<>(this.cacheDatas.values());
	}
	
	/**
	 * getされた回数順にソートされた要素一覧を返します。
	 * @return
	 */
	protected synchronized List<CacheData<T>> getSortedCacheDatasByCallsCount() {
		List<CacheData<T>> result = getCacheDatas();
		result.sort((a, b) -> {
			if(a.getCallsCount() == b.getCallsCount()) {
				if(a.getCallsCurrentTimeMillis() == b.getCallsCurrentTimeMillis()) {
					return Long.compare(a.getCreateCurrentTimeMillis(), b.getCreateCurrentTimeMillis());
				}
				return Long.compare(a.getCallsCurrentTimeMillis(), b.getCallsCurrentTimeMillis());
			}
			return Long.compare(a.getCallsCount(), b.getCallsCount());
		});
		return result;
	}
	
	/**
	 * getされた時間順にソートされた要素一覧を返します。
	 * @return
	 */
	protected synchronized List<CacheData<T>> getSortedCacheDatasByCallsCurrentTimeMillis() {
		List<CacheData<T>> result = getCacheDatas();
		result.sort((a, b) -> {
			if(a.getCallsCurrentTimeMillis() == b.getCallsCurrentTimeMillis()) {
				if(a.getCallsCount() == b.getCallsCount()) {
					return Long.compare(a.getCreateCurrentTimeMillis(), b.getCreateCurrentTimeMillis());
				}
				return Long.compare(a.getCallsCount(), b.getCallsCount());
			}
			return Long.compare(a.getCallsCurrentTimeMillis(), b.getCallsCurrentTimeMillis());
		});
		return result;
	}
	
	/**
	 * putされた時間順にソートされた要素一覧を返します。
	 * @return
	 */
	protected synchronized List<CacheData<T>> getSortedCacheDatasByCreateCurrentTimeMillis() {
		List<CacheData<T>> result = getCacheDatas();
		result.sort((a, b) -> {
			if(a.getCreateCurrentTimeMillis() == b.getCreateCurrentTimeMillis()) {
				if(a.getCallsCount() == b.getCallsCount()) {
					return Long.compare(a.getCallsCount(), b.getCallsCount());
				}
				return Long.compare(a.getCallsCurrentTimeMillis(), b.getCallsCurrentTimeMillis());
			}
			return Long.compare(a.getCreateCurrentTimeMillis(), b.getCreateCurrentTimeMillis());
		});
		return result;
	}
	
	public void addEventListener(CacheEventListener listener) {
		this.cacheEventListeners.add(listener);
	}
}
