package algorithm;
import java.util.ArrayList;

import algorithm.MapUtil.Pair;


public class AccumulatedCostAnalysis { 

	private static class SortedCellList
	{
		//TODO replace with more efficient data structure, such as a self-balancing tree
	    ArrayList<Pair<Integer, Pair<Integer,Integer>>> list;
	    public SortedCellList()
	    {
	        list = new ArrayList<Pair<Integer, Pair<Integer,Integer>>>();
	    }
	    
	    /// Adds cell to list while keeping sorted ordering  
	    public void add(int value, int xLoc, int yLoc)
	    {
	        add(value, new Pair<Integer, Integer>(xLoc, yLoc));
	    }
	    public void add(int value, Pair<Integer, Integer> loc)
	    {
	        // insert into list keeping sorted order
	        int targetIndex = list.size();
	        for(int i = 0; i < list.size(); i ++) {
	            if(list.get(i).getFirst() > value) {
	                targetIndex = i;
	                break;
	            }
	        }
	        
	        Pair<Integer, Pair<Integer,Integer>> toAdd = new Pair<Integer, Pair<Integer,Integer>>(value, loc);
	        list.add(targetIndex, toAdd);
	    }
	    
	    public Pair<Integer, Pair<Integer, Integer>> removeFirst()
	    {
	        if(isEmpty()) return null;
	        return list.remove(0);
	    }
	    
	    public boolean isEmpty()
	    {
	        return list.size() == 0;
	    }
	}

	/*
	 * Calculates the accumulated cost map
	 * @param source 2D array representing the source to which the least cost path from each cell is calculated, where the source cells are given a non-zero value and all others cells are 0.
	 * @param discreteCost 2D representation of discrete cost map 
	 * @param costDistance cost to travel from one cell to the next
	 * @returns 2D array representation of accumulated cost map
	 * @throws IllegalArgumentException if arguments are null or the dimensions of discreteCost and source are not the same (assumes that all columns have the same length) or dimensions are 0
	 */ 
	public static int[][] generateAccumulatedCostMap( int[][] source, int[][] discreteCost, int costDistance)
	{
		if(discreteCost == null || source == null) {
			throw new IllegalArgumentException("Null arguments.");
		}
		
	    if(discreteCost.length == 0 || discreteCost[0].length == 0 || discreteCost.length != source.length || discreteCost[0].length != source[0].length) {
	        throw new IllegalArgumentException("Illegal dimensions of discreteCost and source.");
	    }
	    int width = discreteCost.length;
	    int height = discreteCost[0].length;

	    // where:
	    //      0 means that the cell hasn't been added to toEvaluate yet
	    //      1 means that the cell has been added to toEvaluate but hasn't been evaluated yet
	    //      2 means that the cell has been evaluated
	    int[][] status = new int[width][height];
	   
	    SortedCellList toEvaluate = new SortedCellList();
	    
	    // add all source cells to toEvaluate
	    for(int i = 0; i < source.length; i++) {
	        for(int j = 0; j < source[i].length; j++) {
	            if(source[i][j] != 0) {
	                toEvaluate.add(0, i, j);
	            }
	        }
	    }
	    
	    int[][] accumulatedCost = new int[width][height];
	    
	    while(!toEvaluate.isEmpty()) {
	    	Pair<Integer, Pair<Integer, Integer>> eval = toEvaluate.removeFirst();
	        Pair<Integer, Integer> evalCoor = eval.getSecond();
	        
	        // Check that this cell has not been evaluated yet
	        if(status[evalCoor.getFirst()][evalCoor.getSecond()] == 2) continue;
	        
	        //
	        int accumulatedCellCost = -1;
	        
	        // if source cell
	        if(source[evalCoor.getFirst()][evalCoor.getSecond()] != 0) {
	            accumulatedCellCost = 0;
	        }
	        else {
	        	accumulatedCellCost = eval.getFirst() + discreteCost[evalCoor.getFirst()][evalCoor.getSecond()] + costDistance;
	        }
	        
	        
	        // update accumulatedCost
	        accumulatedCost[evalCoor.getFirst()][evalCoor.getSecond()] = accumulatedCellCost;
	        
	        // update status of eval to evaluated
	        status[evalCoor.getFirst()][evalCoor.getSecond()] = 2;
	        
	        // add neighbours to toEvaluate with a priority of accumulatedCellCost (where lower is higher priority if the neighbour hasn't been evaluated yet
	        // add up
	        if(evalCoor.getSecond() + 1 < height && status[evalCoor.getFirst()][evalCoor.getSecond() + 1] != 2) {
	            toEvaluate.add(accumulatedCellCost, evalCoor.getFirst(), evalCoor.getSecond() + 1);
	            status[evalCoor.getFirst()][evalCoor.getSecond() +  1] = 1;
	        }
	        // add down
	        if(evalCoor.getSecond() - 1 >= 0 && status[evalCoor.getFirst()][evalCoor.getSecond() -  1] != 2) {
	            toEvaluate.add(accumulatedCellCost, evalCoor.getFirst(), evalCoor.getSecond() - 1);
	            status[evalCoor.getFirst()][evalCoor.getSecond() -  1] = 1;
	        }
	        // add right
	        if(evalCoor.getFirst() + 1 < width && status[evalCoor.getFirst() + 1][evalCoor.getSecond()] != 2) {
	            toEvaluate.add(accumulatedCellCost, evalCoor.getFirst() + 1, evalCoor.getSecond());
	            status[evalCoor.getFirst() + 1][evalCoor.getSecond()] = 1;
	        }
	        // add left
	        if(evalCoor.getFirst() - 1 >= 0 && status[evalCoor.getFirst() - 1][evalCoor.getSecond()] != 2) {
	            toEvaluate.add(accumulatedCellCost, evalCoor.getFirst() -  1, evalCoor.getSecond());
	            status[evalCoor.getFirst() - 1][evalCoor.getSecond()] = 1;
	        }
	        
	    }
	    
	    // Return generated accumulatedCost map
	    return accumulatedCost;
	}

	// returns true if it the cell has been evaluated and the accumulatedCost of the cell is smallest
	private static boolean checkCheapest(int x, int y, int[][] status, int[][] accumulatedCost, Pair<Integer, Integer> cheapest)
	{
	    int statusVal = status[x][y];
	    if(statusVal == 2) {
	        if(cheapest == null) return true;
	        else if(accumulatedCost[cheapest.getFirst()][cheapest.getSecond()] > accumulatedCost[x][y]) {
	            return true;
	        }
	    }
	    return false;
	}




}
