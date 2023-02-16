package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper;

public class Pair<E,V> {

	private E first;
	private V second;
	
	public Pair(E e, V v) {
		this.first  = e;
		this.second = v;
	}
	
	public E getFirst() {
		return first;
	}
	
	public V getSecond() {
		return second;
	}
	
}
