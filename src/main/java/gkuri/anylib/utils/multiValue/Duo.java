package gkuri.anylib.utils.multiValue;

public class Duo<T, U>{
	private T first;
	private U second;
	
	public Duo(T first, U second) {
		this.first = first;
		this.second = second;
	}
	
	public T getFirst() {
		return this.first;
	}
	
	public U getSecond() {
		return this.second;
	}
	
	public void setFirst(T first) {
		this.first = first;
	}
	
	public void setSecond(U second) {
		this.second = second;
	}
	
	@Override
	public String toString() {
		return "[ " + this.first + " : " + this.second + " ]";
	}
}
