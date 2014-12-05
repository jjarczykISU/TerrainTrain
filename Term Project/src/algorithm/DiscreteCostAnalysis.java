package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import algorithm.MapUtil.MapTypes;
import algorithm.MapUtil.Pair;

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
				break;
			case ROADS:
				layerCosts.put(layer, roadsLayerCost(layers.get(layer), cellSize));
				break;
			case HOUSINGDENSITY:
				layerCosts.put(layer, housingDensityLayerCost(layers.get(layer)));
				break;
			default: // do nothing unimplemented map type
				break;
			}
		}
		
		// Using weightings to combine map layers into discreteCost map (with special logic for water)
		boolean withWater = layerCosts.containsKey(MapUtil.MapTypes.WATER);
		double[][] waterLayerCost = (withWater) ? layerCosts.get(MapUtil.MapTypes.WATER) : null;
		double[][] discreteCost = new double[width][height]; 
		for (int i = 0; i < width; i ++) {
			for (int j = 0; j < height; j ++) {
				for (MapUtil.MapTypes layer : layerCosts.keySet()) {
					if (withWater && waterLayerCost[i][j] != 0) { // if there is water in this cell
						if (layer == MapTypes.ALTITUDE) continue; // ignore altitude differences layer
					}
					discreteCost[i][j] += weightings.get(layer) * layerCosts.get(layer)[i][j];
				}
			}
		}
		
		return discreteCost;
	}
	
	/**
	 *  Calculates the discrete cost map for the housing density layer (interprets 0 as low housing density up to 255 for high housing density
	 * (1 most preferred, 9 least preferred)
	 * @param housingLayer 2D array representing the housing densities
	 * @return discrete cost map for housing density
	 */
	private static double[][] housingDensityLayerCost(double[][] housingLayer) {
		double[][] cost = new double[housingLayer.length][housingLayer[0].length];
		
		for(int i = 0; i < cost.length; i ++) {
			for(int j = 0; j < cost[0].length; j ++) {
				cost[i][j] = 1 + housingLayer[i][j]*8/255;
			}
		}
		
		return cost;
	}
	
	/**
	 * Calculates the discrete cost map for the roads layer (interprets 0 as no road, otherwise road)
	 * (1 most preferred, 9 least preferred)
	 * @param roadsLayer 2D array representing the roads on the map
	 * @param cellSize dimensions of cell in meters (length/width of cell square)
	 * @return discrete cost map for roads
	 */
	private static double[][] roadsLayerCost(double[][] roadsLayer, double cellSize) {
		int width = roadsLayer.length;
	    int height = roadsLayer[0].length;
		
		// make road distance map (based on accumulated cost code)

	    // where:
	    //      0 means that the cell hasn't been added to toEvaluate yet
	    //      1 means that the cell has been added to toEvaluate but hasn't been evaluated yet
	    //      2 means that the cell has been evaluated
	    int DISCOVERED = 1;
	    int EVALUATED = 2;
	    int[][] status = new int[width][height];
	   
	    SortedCellList toEvaluate = new SortedCellList();
	    
	    // add all source cells to toEvaluate
	    for(int i = 0; i < width; i++) {
	        for(int j = 0; j < height; j++) {
	        	//status[i][j] = UNKNOWN;
	            if(roadsLayer[i][j] <= 0) {
	                toEvaluate.add(0, i, j);
	            }
	        }
	    }
	    
	    double[][] accumulatedCost = new double[width][height];
	    
	    while(!toEvaluate.isEmpty()) {
	    	// get least distance cell from toEvaluate list
	    	Pair<Double, Pair<Integer, Integer>> eval = toEvaluate.removeFirst();
	        Pair<Integer, Integer> evalCoor = eval.getSecond();
	        double distance = eval.getFirst();
	        int x = evalCoor.getFirst();
	        int y = evalCoor.getSecond();
	        
	        // Check that this cell has not been evaluated yet
	        if(status[x][y] == EVALUATED) continue;
	        
	        // update accumulatedCost
	        accumulatedCost[x][y] = Math.abs(10 - distance); //TODO don't hard-code ideal distance (10)
	        
	        // update status of eval to evaluated
	        status[x][y] = EVALUATED;
	        
	        // add neighbor coordinate the accumulated cost of the current cell to toEvalute if that cell does not have the status of 2 yet
	        
	        // add down
	        if(y+1 < height && status[x][y+1] != EVALUATED) {
	            toEvaluate.add(distance + cellSize, x, y+1);
	            status[x][y+1] = DISCOVERED;
	        }
	        // add up
	        if(y-1 >= 0 && status[x][y-1] != EVALUATED) {
	            toEvaluate.add(distance + cellSize, x, y-1);
	            status[x][y-1] = DISCOVERED;
	        }
	        // add right
	        if(x+1 < width && status[x+1][y] != EVALUATED) {
	            toEvaluate.add(distance + cellSize, x+1, y);
	            status[x+1][y] = DISCOVERED;
	        }
	        // add left
	        if(x-1 >= 0 && status[x-1][y] != EVALUATED) {
	            toEvaluate.add(distance + cellSize, x-1, y);
	            status[x-1][y] = DISCOVERED;
	        }
	        // add right-up
	        if((x+1 < width && y-1 >= 0) && status[x+1][y-1] != EVALUATED) {
	        	toEvaluate.add(distance + cellSize*Math.sqrt(2), x+1, y-1);
	            status[x+1][y-1] = DISCOVERED;
	        }
	        // add left-up
	        if((x-1 >= 0 && y-1 >= 0) && status[x-1][y-1] != EVALUATED) {
	            toEvaluate.add(distance + cellSize*Math.sqrt(2), x-1, y-1);
	            status[x-1][y-1] = DISCOVERED;
	        }
	        //add right-down
	        if((x+1 < width && y+1 < height) && status[x+1][y+1] != EVALUATED) {
	            toEvaluate.add(distance + cellSize*Math.sqrt(2), x+1, y+1);
	            status[x+1][y+1] = DISCOVERED;
	        }
	        //add left-down
	        if((x-1 >= 0 && y+1 < height) && status[x-1][y+1] != EVALUATED) {
	            toEvaluate.add(distance + cellSize*Math.sqrt(2), x-1, y+1);
	            status[x-1][y+1] = DISCOVERED;
	        }
	    }
	    
		return accumulatedCost;
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
	 * 9 -> >=60 degrees or <=-50 degrees
	 * otherwise given a value using an exponential function with range 1 to 8
	 * @param cellSize dimension of cell in meters
	 * @param altitudeScale scale factor for altitude
	 * @return the preference of the slope of the cell given the neighboring cells
	 * @throws IllegalArgumentException neighbors is null or if the number of neighbors is 1 or less
	 */
	private static double slopePreference(double altitudeLayer, ArrayList<Double> neighbors, double cellSize, double altitudeScale) {
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
		
		double preference = 9.0;
		if(greatestSlope <= 60 && smallestSlope > -50) {
			if(greatestSlope < 0) preference = 1.0;
			else preference = 1 + Math.pow(greatestSlope, 4)/Math.pow(60,4)*8;
			
		}
		
		return preference;

		/* Another continuous function:
		 * 
		double avgDiff = 0;
		for(Double alt : neighbors) {
			avgDiff += Math.abs(altitudeLayer - alt);
		}
		avgDiff = avgDiff / neighbors.size() * altitudeScale;

		double avgPercentGrade = 100.0 * avgDiff / cellSize;
		
		// More than 100% grade (45 degrees) is patently absurd.
		// The steepest train track in the world is 13.5%. The steepest car road in the world is about 35%.
		// http://en.wikipedia.org/wiki/Grade_%28slope%29#Railways
		double limit = 25.0;
		if (avgPercentGrade > limit) return 9.0;
		return 1.0 + 8.0 * Math.pow(avgPercentGrade, 2.0) / Math.pow(limit, 2.0); //can change the power to alter the shape of the curve.
		*/
	}
	

}
