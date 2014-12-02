package algorithm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import algorithm.MapUtil.Pair;
import fileUtils.FileUtil;

public class MapAnalysis {	
	// Cost to traverse a cell 
	private int COSTDISTANCE = 1;
	
	public int[][] discreteCost;
	public int[][] accumulatedCost;
	public int[][] path;
	
	public MapAnalysis(int[][] source, List<int[][]> layers, Pair<Integer, Integer> destination) {	
		// Discrete cost analysis
		discreteCost =  DiscreteCostAnalysis.generateDiscreteCostMap(layers);
		
		// Accumulated cost analysis
		accumulatedCost = AccumulatedCostAnalysis.generateAccumulatedCostMap(source, discreteCost, COSTDISTANCE);
		
		// Steepest Cost Path analysis
		path = steepestCostPath(destination.getFirst(),destination.getSecond(), accumulatedCost);
		
	}
	
	private static int[][] steepestCostPath(int x, int y, int[][] accumulatedCost) {
		int width = accumulatedCost.length;
		int height = accumulatedCost[0].length;
		int[][] path = new int[width][height];
		
		int currValue = accumulatedCost[x][y];
		path[x][y] = 1;
		
		while(currValue != 0) {
			Pair<Integer, Integer> nextCell = null;
			int smallestValue = -1;
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
			// Left
			if(x + 1 < accumulatedCost.length && path[x + 1][y] != 1) {
				if(smallestValue == -1 || accumulatedCost[x + 1][y] < smallestValue) {
					smallestValue = accumulatedCost[x + 1][y];
					nextCell = new Pair<Integer, Integer>(x + 1, y);
				}
			}
			// Right
			if(x - 1 >= 0 && path[x -  1][y] != 1) {
				if(smallestValue == -1 || accumulatedCost[x - 1][y] < smallestValue) {
					smallestValue = accumulatedCost[x - 1][y];
					nextCell = new Pair<Integer, Integer>(x - 1, y);
				}
			}
			
			x = nextCell.getFirst();
			y = nextCell.getSecond();
			currValue = accumulatedCost[x][y];
			path[x][y] = 1;
		}
		
		return path;
	}

}

