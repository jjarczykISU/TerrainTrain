package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DiscreteCostAnalysis {

	/**
	 * Calculates the discrete cost map given a list of of map layers
	 * @param layers mapping MapTypes to 2D arrays representing the map data to be analyzed
	 * @param cellSize dimensions of cell in meters (length/width of cell square)
	 * @param altitudeScale scale factor for altitude
	 * @param weightings mapping of MapTypes to a double weighting
	 * @throws IllegalArgumentException
	 */
	public static double[][] generateDiscreteCostMap(Map<MapUtil.MapTypes, double[][]> layers, double cellSize, double altitudeScale, Map<MapUtil.MapTypes, Double> weightings) {
		
		// Check that arguments are valid
		// Check for null arguments
		if(layers == null) {
			throw new IllegalArgumentException("Null arguments.");
		}
		// Check for mismatching dimensions
		int width = -1;
		int height = -1;
		for(double[][] layer : layers.values()) {
			if(width == -1 || height == -1) {
				width = layer.length;
				if(width == 0) throw new IllegalArgumentException("Illegal dimensions for layers.");
				height = layer[0].length;
				if(height == 0) throw new IllegalArgumentException("Illegal dimensions for layers.");
			} else {
				if(layer.length != width || layer[0].length != height) {
					throw new IllegalArgumentException("Mismatching layer dimensions.");
				}
			}
		}
		// Checks that all layers have a weighting
		for(MapUtil.MapTypes layer : layers.keySet()) {
			if(!weightings.containsKey(layer)) {
				throw new IllegalArgumentException("Missing later weighting.");
			}
				
		}
		
		Map<MapUtil.MapTypes, double[][]> layerCosts = new HashMap<MapUtil.MapTypes, double[][]>();
		// Calculate cost for each layer relative to itself (with each cell valued 1 to 9 inclusive
		for(MapUtil.MapTypes layer : layers.keySet()) {
			switch (layer) {
			case ALTITUDE:
				layerCosts.put(layer, altitudeLayerCost(layers.get(layer), cellSize, altitudeScale));
				break;
			case WATER:
				layerCosts.put(layer, waterLayerCost(layers.get(layer), altitudeScale));
			default: // do nothing unimplemented map type
				break;
			}
		}
		
		// Using weightings to combine map layers into discreteCost map (with special logic for water)
		boolean withWater = layerCosts.containsKey(MapUtil.MapTypes.WATER);
		double[][] waterLayerCost = (withWater) ? layerCosts.get(MapUtil.MapTypes.WATER) : null;
		double waterLayerWeighting = (withWater) ? weightings.get(MapUtil.MapTypes.WATER) : 0.0;
		double[][] discreteCost = new double[width][height]; 
		for(int i = 0; i < width; i ++) {
			for(int j = 0; j < height; j ++) {
				if(withWater && waterLayerCost[i][j] != 0) { // if there is water in this cell
					discreteCost[i][j] = waterLayerWeighting*waterLayerCost[i][j]*(layerCosts.size() - 1); // weighting * discreteCost * number of layers included in land regions
				} else {
					for(MapUtil.MapTypes layer : layerCosts.keySet()) {
						if(layer != MapUtil.MapTypes.WATER) {
							discreteCost[i][j] += weightings.get(layer)*layerCosts.get(layer)[i][j];
						}
					}
				}
			}
		}
		
		return discreteCost;
	}
	
	/**
	 * Calculates the discrete cost map for the water layer (interprets values as the depth of the water)
	 * (1 most preferred, 9 least preferred)
	 * @param waterLayer 2D array representing the water bodies on the map
	 * @param altitudeScale scale factor for altitude
	 * @return discrete cost map for water bodies
	 */
	private static double[][] waterLayerCost(double[][] waterLayer, double altitudeScale) {
		double[][] cost = new double[waterLayer.length][waterLayer[0].length];
		
		for(int i = 0; i < cost.length; i ++) {
			for(int j = 0; j < cost[0].length; j ++) {
				if(waterLayer[i][j] != 0) {
					if(waterLayer[i][j]*altitudeScale > 200) // if the support for the bridge would be greater that 200 meters
						cost[i][j] = 9;
					else
						cost[i][j] = 2 + waterLayer[i][j]*altitudeScale/200*6; // base cost penalty for building bridge + additional cost for depth of supports
				}
				else cost[i][j] = 0;
			}
		}
		
		return cost;
	}
	
	/**
	 * Calculates the discrete cost map for the altitude layer
	 * (1 most preferred, 9 least preferred)
	 * @param altitudeLayer 2D array representing the altitudes of the map
	 * @param cellSize dimensions of cell in meters (length/width of cell square)
	 * @param altitudeScale scale factor for altitude
	 * @return discrete cost map for altitude
	 */
	private static double[][] altitudeLayerCost(double[][] altitudeLayer, double cellSize, double altitudeScale) 
	{
		double[][] cost = new double[altitudeLayer.length][altitudeLayer[0].length];
		
		for(int i = 0; i < cost.length; i ++) {
			for(int j = 0; j < cost[0].length; j ++) {
				// List of altitudes of neighbor cells 
				ArrayList<Double> neighbors = new ArrayList<Double>();
				// Up
				if(j + 1 < cost[0].length) neighbors.add(altitudeLayer[i][j + 1]);
				// Down
				if(j - 1 >= 0) neighbors.add(altitudeLayer[i][j - 1]);
				// Right
				if(i + 1 < cost.length) neighbors.add(altitudeLayer[i + 1][j]);
				// Left
				if(i - 1 >= 0) neighbors.add(altitudeLayer[i - 1][j]);
				
				cost[i][j] = slopePreference(altitudeLayer[i][j], neighbors, cellSize, altitudeScale);				
			}
		}
		
		return cost;
	}
	/**
	 * Preference cost for altitude differences (angle between neighbors) for a single cell.
	 * (1 most preferred, 9 least preferred):
	 * 1 -> <15 degrees and >-50 degrees
	 * 2 -> <30 degrees and >-50 degrees
	 * 3 -> <45 degrees and >-50 degrees
	 * 4 -> <50 degrees and >-50 degrees
	 * 9 -> >60 degrees or <=-50 degrees
	 * @param cellSize dimension of cell in meters
	 * @param altitudeScale scale factor for altitude
	 * @return the preference of the slope of the cell given the neighboring cells
	 * @throws IllegalArgumentException neighbors is null or if the number of neighbors is 1 or less
	 */
	private static int slopePreference(double altitudeLayer, ArrayList<Double> neighbors, double cellSize, double altitudeScale) {
		if(neighbors == null || neighbors.size() <= 1) {
			throw new IllegalArgumentException();
		}
		double greatestSlope = 0;
		double smallestSlope = Integer.MAX_VALUE;

		for(Double alt : neighbors) {
			double altDiff = (altitudeLayer - alt)*altitudeScale;
			double calcSlope = altDiff/cellSize;
			calcSlope = Math.toDegrees(Math.atan(calcSlope));			
			
			if(calcSlope > greatestSlope) greatestSlope = calcSlope;
			if(calcSlope < smallestSlope) smallestSlope = calcSlope;
		}
		
		//TODO low precision and aliasing, does not recognize small differences.
		int preference = 9;
		if(greatestSlope < 15 && smallestSlope >= -50) { 
			preference = 1;
		} else if(greatestSlope < 30 && smallestSlope >= -50){
			preference = 2;
		} else if(greatestSlope < 45 && smallestSlope >= -50){
			preference = 3;
		} else if(greatestSlope < 50){
			preference = 4;
		}
		
		return preference;
	}
	

}
