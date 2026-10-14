package gkuri.anylib.utils.multiValue;

public class Quart<T, U, V, W>{
	
	private T first;
	private U second;
	private V therd;
	private W fourth;
	
	public Quart(T first, U second, V therd, W fourth) {
		this.first = first;
		this.second = second;
		this.therd = therd;
		this.fourth = fourth;
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
	
	public W getFourth() {
		return this.fourth;
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
	
	public void setFourth(W fourth) {
		this.fourth = fourth;
	}
	
	@Override
	public String toString() {
		return "[ " + this.first + " : " + this.second + " : " + this.therd + " : " + this.fourth + " ]";
	}
}