package gkuri.anylib.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BigList<T> implements Iterable<T>{
	
	private int chunks;
	private ArrayList<ArrayList<T>> list = new ArrayList<>();
	private final int MAX_SIZE;
	private long size;
	
	public static <U> BigList<U> of(BigList<U> oldList){
		BigList<U> newList = new BigList<>();
		newList.chunks = oldList.chunks;
		newList.size = oldList.size;
		newList.list = oldList.list;
		return newList;
	}
	
 	public BigList() {
		this.chunks = 0;
		this.size = 0;
		this.MAX_SIZE = 2_100_000_000;
		this.list.add(new ArrayList<T>());
	}
	
	public long size() {
		return this.size;
	}
	
	public boolean isEmpty() {
		return this.size == 0;
	}
	
	public T get(long index) {
		int chunk = (int)(index / MAX_SIZE);
		int offset = (int)(index % MAX_SIZE);

		return this.list.get(chunk).get(offset);
	}

	public T getFirst(){
		return this.list.getFirst().getFirst();
	}

	public T getLast(){
		return this.list.getLast().getLast();
	}
	
	public boolean add(T value) {
		if(this.list.get(this.chunks).size() < MAX_SIZE) {
			this.list.get(this.chunks).add(value);
		} else {
			this.list.add(new ArrayList<T>());
			this.chunks++;
			this.list.get(this.chunks).add(value);
		}
		this.size++;
		return true;
	}
	
	public boolean addAll(T[] values) {
		for(T v : values) {
			if(!add(v)) return false;
		}
		return true;
	}
	
	public boolean addAll(List<T> values) {
		for(T v : values) {
			if(!add(v)) return false;
		}
		return true;
	}
	
	public boolean addAll(BigList<T> values) {
		for(long i = 0; i < values.size(); i++) {
			if(!add(values.get(i))) return false;
		}
		return true;
	}
	
	public T set(long index, T value) {
		int chunk = (int)(index / MAX_SIZE);
		int offset = (int)(index % MAX_SIZE);

		T oldValue = this.list.get(chunk).get(offset);
		this.list.get(chunk).set(offset, value);
		return oldValue;
	}
	
	public boolean remove(long index) {
		int chunk = (int)(index / MAX_SIZE);
		int offset = (int)(index % MAX_SIZE);
		
		this.list.get(chunk).remove(offset);
		
		for (int i = chunk; i < this.list.size() - 1; i++) {
	        ArrayList<T> next = this.list.get(i + 1);
	        if (!next.isEmpty()) {
	        	this.list.get(i).add(next.remove(0));
	        }
	    }

	    if (this.list.get(this.list.size() - 1).isEmpty() && 1 < this.list.size()) {
	        this.list.remove(this.list.size() - 1);
	        this.chunks--;
	    }
	    this.size--;
	    return true;
	}

	public long indexOf(T value) {
		for(int i = 0; i < list.size(); i++){
			int index = list.get(i).indexOf(value);
			if(index != -1){
				return (long)MAX_SIZE * i + index;
			}
		}
		return -1;
	}
	
	public boolean clear() {
		this.list.clear();
		this.list.add(new ArrayList<T>());
		this.chunks = 0;
		this.size = 0;
		return true;
	}

	@Override
    public Iterator<T> iterator() {
        return new BigListIterator();
    }

	private class BigListIterator implements Iterator<T> {

        int chunkIndex = 0;
		int innerIndex = 0;
		long unused = size;

		@Override
		public boolean hasNext() {
			return 0 < unused;
		}

		@Override
		public T next() {
			ArrayList<T> chunk = list.get(chunkIndex);
			T value = chunk.get(innerIndex++);

			if(innerIndex >= chunk.size()){
				chunkIndex++;
				innerIndex = 0;
			}
			unused--;
			return value;
		}
	}
}