 package Structures;


public class Pair<T> implements Comparable<Pair> {
	private double key;
	private T value;
	
	public Pair(double pair_key,T pair_value) {
		key=pair_key;
		value=pair_value;
	}
	
	public T getValue() {
		return value;
	}
	public double getKey() {
		return key;
	}
	@Override
	public int compareTo(Pair o) {
		return Double.compare(key, o.key);
	}

	
}
