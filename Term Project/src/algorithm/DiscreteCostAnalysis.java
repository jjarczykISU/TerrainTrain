package algorithm;

import java.util.ArrayList;
import java.util.List;

public class DiscreteCostAnalysis {

	/**
	 * Calculates the discrete cost map given a list of of map layers
	 * @param layers list of 2D arrays whether the first array is the altitude map and the second is the water body map
	 * @throws IllegalArgumentException if arguments are null or if dimensions of layers are 0 or do not match
	 */
	public static double[][] generateDiscreteCostMap(List<int[][]> layers) {
		
		// Check that arguments are valid
		if(layers == null) {
			throw new IllegalArgumentException("Null arguments.");
		}
		if(layers.get(0) == null) {
			throw new IllegalArgumentException("Null arguments.");
		}
		int width = layers.get(0).length;
		if(width == 0) throw new IllegalArgumentException("Illegal dimensions for layers.");
		int height = layers.get(0)[0].length;
		if(height == 0) throw new IllegalArgumentException("Illegal dimensions for layers.");
		for(int i = 1; i < layers.size(); i ++) {
			if(layers.get(i) == null) {
				throw new IllegalArgumentException("Null arguments.");
			}
			if(layers.get(i).length != width || layers.get(i)[0].length != height) {
				throw new IllegalArgumentException("Mismatching layer dimensions.");
			}
		}
		
		// Calculate cost for each layer relative to itself (with each cell valued 1 to 9 inclusive
		
		List<int[][]> layerCosts = new ArrayList<int[][]>();
		// First layer is interpreted as altitudes
		layerCosts.add(altitudeLayerCost(layers.get(0), MapUtil.CELLSIZE));
		// Second layer is interpreted as water bodies
		layerCosts.add(waterLaterCost(layers.get(1)));
		
		//TODO create a table for weightings?
		//TODO add more layer logic
		
		// Using weightings to combine map layers into discreteCost map
		
		double[][] discreteCost = new double[width][height]; 
		for(int i = 0; i < width; i ++) {
			for(int j = 0; j < height; j ++) {
				if(layerCosts.get(1)[i][j] != 0) // if there is water and requires a bridge
					discreteCost[i][j] = layerCosts.get(1)[i][j];
				else discreteCost[i][j] = layerCosts.get(0)[i][j];	
			}
		}
		
		// TODO path straightening preference adjustment http://www.innovativegis.com/basis/mapanalysis/topic19/topic19.htm
		
		return discreteCost;
	}
	
	/**
	 * Calculates the discrete cost map for the water layer
	 * (1 most preferred, 9 least preferred)
	 * @param waterLater 2D array representing the water bodies on the map
	 * @return discrete cost map for water bodies
	 */
	private static int[][] waterLaterCost(int[][] waterLater) {
		int[][] cost = new int[waterLater.length][waterLater[0].length];
		
		for(int i = 0; i < cost.length; i ++) {
			for(int j = 0; j < cost[0].length; j ++) {
				if(waterLater[i][j] != 0) cost[i][j] = waterLater[i][j]*9/255;
				else cost[i][j] = 0;
			}
		}
		
		return cost;
	}
	
	/**
	 * Calculates the discrete cost map for the altitude layer
	 * @param altitudeLayer 2D array representing the altitudes of the map
	 * @param cellSize number of units representing the length/width of a single cell
	 * @return discrete cost map for altitude
	 */
	private static int[][] altitudeLayerCost(int[][] altitudeLayer, double cellSize) 
	{
		int[][] cost = new int[altitudeLayer.length][altitudeLayer[0].length];
		
		for(int i = 0; i < cost.length; i ++) {
			for(int j = 0; j < cost[0].length; j ++) {
				// List of altitudes of neighbor cells 
				ArrayList<Integer> neighbors = new ArrayList<Integer>();
				// Up
				if(j + 1 < cost[0].length) neighbors.add(altitudeLayer[i][j + 1]);
				// Down
				if(j - 1 >= 0) neighbors.add(altitudeLayer[i][j - 1]);
				// Right
				if(i + 1 < cost.length) neighbors.add(altitudeLayer[i + 1][j]);
				// Left
				if(i - 1 >= 0) neighbors.add(altitudeLayer[i - 1][j]);
				
				cost[i][j] = slopePreference(altitudeLayer[i][j], neighbors, cellSize);				
			}
		}
		
		return cost;
	}
	/**
	 * Preference cost for altitude differences (angle between neighbors) for a single cell
	 * (1 most preferred, 9 least preferred):
	 * 1 -> <15 degrees and >-50 degrees
	 * 2 -> <30 degrees and >-50 degrees
	 * 3 -> <45 degrees and >-50 degrees
	 * 6 -> <50 degrees and >-50 degrees
	 * 9 -> >60 degrees or <=-50 degrees
	 * @param cellSize dimension of cell
	 * @return the preference of the slope of the cell given the neighboring cells
	 * @throws IllegalArgumentException neighbors is null or if the number of neighbors is 1 or less
	 */
	private static int slopePreference(int cellAltitude, ArrayList<Integer> neighbors, double cellSize) {
		if(neighbors == null || neighbors.size() <= 1) {
			throw new IllegalArgumentException();
		}
		double greatestSlope = 0;
		double smallestSlope = Integer.MAX_VALUE;

		for(Integer alt : neighbors) {
			double calcSlope = cellAltitude - alt;
			calcSlope /= (cellSize);
			calcSlope = Math.toDegrees(Math.atan(calcSlope));			
			
			if(calcSlope > greatestSlope) greatestSlope = calcSlope;
			if(calcSlope < smallestSlope) smallestSlope = calcSlope;
		}
		
		int preference = 9;
		if(greatestSlope < 15 && smallestSlope >= -50) { 
			preference = 1;
		} else if(greatestSlope < 30 && smallestSlope >= -50){
			preference = 2;
		} else if(greatestSlope < 45 && smallestSlope >= -50){
			preference = 3;
		} else if(greatestSlope < 50){
			preference = 6;
		}
		
		return preference;
	}
	

}
