package algorithm;

public class MapUtil {
	public static double CELLSIZE = 30.0;
	
	public static class Pair<A,B> {
	    private A first;
	    private B second;
	    public Pair(A first, B second)
	    {
	        this.first = first;
	        this.second = second;
	    }
	    public A getFirst() { return first; }
	    public B getSecond() { return second; }
	}
	
}
