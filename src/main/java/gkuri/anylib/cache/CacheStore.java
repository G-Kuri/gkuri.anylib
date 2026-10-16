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
 * 実際にデータが保存されるのは
 * Map<String, CacheData<T>> cacheDatas
 * です。
 * イベントリスナは
 * List<CacheEventListener> cacheEventListeners
 * です。
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
	
	/**
	 * データをキーに紐づけて保存又は更新します。
	 * @param key
	 * @param data
	 */
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
	
	/**
	 * キャッシュデータを手動で削除します。
	 * @param key
	 */
	public synchronized void remove(String key) {
		CacheData<T> cacheData = this.cacheDatas.remove(key);
		if(cacheData != null) {
			this.removeData(cacheData);
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onRemove(cacheData);
			}
		}
	} 
	
	/**
	 * キャッシュに保存されたすべてのデータを削除します。
	 */
	public synchronized void clear() {
		this.clearData();
		this.cacheDatas.clear();
		for(CacheEventListener listener : cacheEventListeners) {
			listener.onClear();
		}
	}
	
	/**
	 * キャッシュに保存されたデータを返します。
	 * データが見つからない、又は無効になっている場合はNULLを返します。
	 * @param key
	 * @return
	 */
	public synchronized T get(String key){
		CacheData<T> data = this.cacheDatas.get(key);
		if(data == null) return null;
		
		if(this.getData(data)) {
			data.incCallsCount();
			data.setCallsCurrentTimeMillisNow();
			
			for(CacheEventListener listener : cacheEventListeners) {
				listener.onGet(data);
			}
			return data.getData();
		}
		return null;
	}
	
	/**
	 * キャッシュに保存されたデータに紐づくキーの一覧を返します。
	 * @return
	 */
	public Set<String> keySet(){
		return this.cacheDatas.keySet();
	}
	
	/**
	 * putした際に呼ばれるので、必要に応じてcacheDatasから削除する処理を書くことができます。
	 * 呼び出しはデータをcacheDatasに保存する直前です。
	 * @param data
	 */
	abstract protected void putData(CacheData<T> data);
	
	/**
	 * getした際に呼ばれるので、何か実行したい処理がある場合はOverrideしてください。
	 * 呼び出し回数と最終呼出時間の更新はCacheStoreで行うため不要です。
	 * falseを返すとgetの戻り値を無効な値としてNULLに変えられますが、データは削除されないため
	 * クリンナップする場合はその処理も記載してください。
	 * また、getが呼ばれても対象のデータがなけれ呼び出されません。
	 * @param data
	 */
	protected boolean getData(CacheData<T> data) {
		return true;
	}
	
	/**
	 * removeした際に呼ばれるので、何か実行したい処理がある場合はOverrideしてください。
	 * 呼び出しはデータをcacheDatasから削除された直後です。
	 * また、removeが呼ばれても削除する対象のデータがなけれ呼び出されません。
	 * @param data
	 */
	protected void removeData(CacheData<T> data) {
		
	}
	
	/**
	 * clearした際に呼ばれるので、何か実行したい処理がある場合はOverrideしてください。
	 * 呼び出しはcacheDatasをクリアする直前です。
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
