package algorithm;

public class MapUtil {
	// Used in AccumulatedCostAnalysis, constant that represents the length/width of a cell proportional to the altitude (more formally: CELLSIZE = value *255/(altitude value on map that represents black))
	public static double CELLSIZE = 30.0;
	
	// Enums for types of maps
	public enum MapTypes{ ALTITUDE, WATER, HOUSINGDENSITY, ROADS };
	
	// Data structure for pairings of objects
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
