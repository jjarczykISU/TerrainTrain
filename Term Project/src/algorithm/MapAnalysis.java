package algorithm;

import java.util.Map;

import algorithm.MapUtil.Pair;

public class MapAnalysis {		
	public double[][] discreteCost;
	public double[][] accumulatedCost;
	public int[][] path;
	
	/**
	 * Generates a discrete cost map, accumulated cost map, and an optimal path given the geographical information of a map.
	 * @param source ending area for the path represented as a 2D array where a non-zero represents a potential ending area
	 * @param start starting point for the path
	 * @param layers mapping of a map type to the map information
	 * @param cellSize dimensions of cell in meters (length/width of cell square)
	 * @param altitudeScale scale factor for altitude
	 * @param costDistance cost to traverse cell
	 * @param weightings mapping of a map type to a weighting
	 */
	public MapAnalysis(int[][] source, Pair<Integer, Integer> start, Map<MapUtil.MapTypes, double[][]> layers, double cellSize, double altitudeScale, double costDistance, Map<MapUtil.MapTypes, Double> weightings) {	
		// Discrete cost analysis
		discreteCost =  DiscreteCostAnalysis.generateDiscreteCostMap(layers, cellSize, altitudeScale, weightings);
		
		// Accumulated cost analysis
		accumulatedCost = AccumulatedCostAnalysis.generateAccumulatedCostMap(source, discreteCost, costDistance);
		
		// Steepest Cost Path analysis
		path = steepestCostPath(start, accumulatedCost);
		
	}
	
	/**
	 * Using the greedy algorithm of choosing the next neighbor with the least accumulatedCost value to be the next path segment and generates an optimal path 
	 * (It is called the steepest Cost Path because the accumulatedCost map can be considered like an altitude map where we are going from a high altitude to ground level and generate a path that takes to sttepest path segments)
	 * @param start starting point for the path
	 * @param accumulatedCost the accumulated cost map for the area (assumes that this s a valid accumuatedCost map and so there is at least one cell with a value of 0)
	 * @return the optimal path represented as a 2D array where a non-zero represents a path segment
	 */
	private static int[][] steepestCostPath(Pair<Integer, Integer> start, double[][] accumulatedCost) {
		int x = start.getFirst();
		int y = start.getSecond();
		
		// Initialize path
		int width = accumulatedCost.length;
		int height = accumulatedCost[0].length;
		int[][] path = new int[width][height];
		
		double currValue = accumulatedCost[x][y];
		path[x][y] = 1;
		
		// Until the path has reached the source (where the source cells have values of 0)
		while(currValue != 0) {
			// Determine the next path segment by finding the neighbor with the least cost
			Pair<Integer, Integer> nextCell = null;
			double smallestValue = -1;
			// Down
			if(y + 1 < accumulatedCost[0].length && path[x][y + 1] != 1) {
				if(smallestValue == -1 || accumulatedCost[x][y + 1] < smallestValue) {
					smallestValue = accumulatedCost[x][y + 1];
					nextCell = new Pair<Integer, Integer>(x, y + 1);
				}
			}
			// Up
			if(y - 1 >= 0 && path[x][y - 1] != 1) {
				if(smallestValue == -1 || accumulatedCost[x][y - 1] < smallestValue) {
					smallestValue = accumulatedCost[x][y - 1];
					nextCell = new Pair<Integer, Integer>(x, y - 1);
				}
			}
			// Right
			if((x + 1 < accumulatedCost.length) && path[x + 1][y] != 1) {
				if(smallestValue == -1 || accumulatedCost[x + 1][y] < smallestValue) {
					smallestValue = accumulatedCost[x + 1][y];
					nextCell = new Pair<Integer, Integer>(x + 1, y);
				}
			}
			// Left
			if((x - 1 >= 0) && path[x -  1][y] != 1) {
				if(smallestValue == -1 || accumulatedCost[x - 1][y] < smallestValue) {
					smallestValue = accumulatedCost[x - 1][y];
					nextCell = new Pair<Integer, Integer>(x - 1, y);
				}
			}
			// Down-Right
			if(y + 1 < accumulatedCost[0].length && (x + 1 < accumulatedCost.length) && path[x + 1][y + 1] != 1) {
				if(smallestValue == -1 || accumulatedCost[x + 1][y + 1] < smallestValue) {
					smallestValue = accumulatedCost[x + 1][y + 1];
					nextCell = new Pair<Integer, Integer>(x + 1, y + 1);
				}
			}
			// Down-Left
			if(y + 1 < accumulatedCost[0].length && (x - 1 >= 0) && path[x - 1][y + 1] != 1) {
				if(smallestValue == -1 || accumulatedCost[x - 1][y + 1] < smallestValue) {
					smallestValue = accumulatedCost[x - 1][y + 1];
					nextCell = new Pair<Integer, Integer>(x - 1, y + 1);
				}
			}
			// Up-Right
			if(y - 1 >= 0 && (x + 1 < accumulatedCost.length) && path[x + 1][y - 1] != 1) {
				if(smallestValue == -1 || accumulatedCost[x + 1][y - 1] < smallestValue) {
					smallestValue = accumulatedCost[x + 1][y - 1];
					nextCell = new Pair<Integer, Integer>(x + 1, y - 1);
				}
			}
			// Up-Left
			if(y - 1 >= 0 && (x - 1 >= 0) && path[x - 1][y - 1] != 1) {
				if(smallestValue == -1 || accumulatedCost[x - 1][y - 1] < smallestValue) {
					smallestValue = accumulatedCost[x - 1][y - 1];
					nextCell = new Pair<Integer, Integer>(x - 1, y - 1);
				}
			}
			
			// Adds path segment to the path
			x = nextCell.getFirst();
			y = nextCell.getSecond();
			path[x][y] = 1;
			currValue = accumulatedCost[x][y];
		}
		
		return path;
	}

}

