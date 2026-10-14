package gkuri.anylib.cache.events;

import java.util.List;

import gkuri.anylib.cache.CacheData;

/**
 * CacheStoreの各イベントを購読するためのインターフェイスです。
 */
public interface CacheEventListener {
	
	/**
	 * データがputされた時に発火するイベントです。
	 * @param data
	 */
	default public void onPut(CacheData<?> data) {
		
	}
	
	/**
	 * データがputされた時に発火するイベントです。
	 * 但し、データが見つからなかった場合は発火しません。
	 * @param data
	 */
	default public void onGet(CacheData<?> data) {
		
	}
	
	/**
	 * データが手動removeされた時に発火するイベントです。
	 * 但し、削除対象データが見つからなかった場合は発火しません。
	 * @param data
	 */
	default public void onRemove(CacheData<?> data) {
		
	}
	
	/**
	 * データが手動クリアされた時に発火するイベントです。
	 */
	default public void onClear() {
		
	}
	
	/**
	 * データが自動削除された時に発火するイベントです。
	 * 但し、CacheStoreの子クラスがこのイベントをサポートする必要があります。
	 * @param data
	 */
	default public void onCleanup(List<CacheData<?>> data) {
		
	}
}
