package gkuri.anylib.utils.multiValue;

public class Trio<T, U , V>{
	
	private T first;
	private U second;
	private V therd;
	
	public Trio(T first, U second, V therd){
		this.first = first;
		this.second = second;
		this.therd = therd;
	}
	
	public T getFirst() {
		return this.first;
	}
	
	public U getSecond() {
		return this.second;
	}
	
	public V getTherd() {
		return this.therd;
	}
	
	public void setFirst(T first) {
		this.first = first;
	}
	
	public void setSecond(U second) {
		this.second = second;
	}
	
	public void setTherd(V therd) {
		this.therd = therd;
	}
	
	@Override
	public String toString() {
		return "[ " + this.first + " : " + this.second + " : " + this.therd + " ]";
	}
}