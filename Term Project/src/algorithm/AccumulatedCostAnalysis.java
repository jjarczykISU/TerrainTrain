package algorithm;
import java.util.PriorityQueue;

import algorithm.MapUtil.Pair;


public class AccumulatedCostAnalysis { 

	private static class SortedCellList
	{
	    PriorityQueue<Pair<Double, Pair<Integer,Integer>>> list;
	    public SortedCellList()
	    {
	        list = new PriorityQueue<Pair<Double, Pair<Integer,Integer>>>(10, new CellComparator());
	    }
	    
	    /// Adds cell to queue
	    public void add(double value, int xLoc, int yLoc)
	    {
	        add(value, new Pair<Integer, Integer>(xLoc, yLoc));
	    }
	    public void add(double value, Pair<Integer, Integer> loc)
	    {
	        Pair<Double, Pair<Integer,Integer>> toAdd = new Pair<Double, Pair<Integer,Integer>>(value, loc);
	        list.add(toAdd);
	    }
	    
	    public Pair<Double, Pair<Integer, Integer>> removeFirst()
	    {
	        if(list.isEmpty()) return null;
	        return list.remove();
	    }
	    
	    public boolean isEmpty()
	    {
	        return list.isEmpty();
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
	public static double[][] generateAccumulatedCostMap( int[][] source, double[][] discreteCost, int costDistance)
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
	    int DISCOVERED = 1;
	    int EVALUATED = 2;
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
	    
	    double[][] accumulatedCost = new double[width][height];
	    
	    while(!toEvaluate.isEmpty()) {
	    	Pair<Double, Pair<Integer, Integer>> eval = toEvaluate.removeFirst();
	        Pair<Integer, Integer> evalCoor = eval.getSecond();
	        
	        // Check that this cell has not been evaluated yet
	        if(status[evalCoor.getFirst()][evalCoor.getSecond()] == EVALUATED) continue;
	        
	        double accumulatedCellCost;
	        
	        // if source cell
	        if(source[evalCoor.getFirst()][evalCoor.getSecond()] != 0) {
	            accumulatedCellCost = 0;
	        }
	        else {
	        	// Accumulated Cost = (accumulated cost of neighbor + cost of traversing a cell) + discrete cost of cell
	        	accumulatedCellCost = eval.getFirst() + discreteCost[evalCoor.getFirst()][evalCoor.getSecond()] + costDistance;
	        }
	        
	        
	        // update accumulatedCost
	        accumulatedCost[evalCoor.getFirst()][evalCoor.getSecond()] = accumulatedCellCost;
	        
	        // update status of eval to evaluated
	        status[evalCoor.getFirst()][evalCoor.getSecond()] = EVALUATED;
	        
	        // add neighbor coordinate the accumulated cost of the current cell to toEvalute if that cell does not have the status of 2 yet
	        // add down
	        if(evalCoor.getSecond() + 1 < height && status[evalCoor.getFirst()][evalCoor.getSecond() + 1] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance, evalCoor.getFirst(), evalCoor.getSecond() + 1);
	            status[evalCoor.getFirst()][evalCoor.getSecond() +  1] = DISCOVERED;
	        }
	        // add up
	        if(evalCoor.getSecond() - 1 >= 0 && status[evalCoor.getFirst()][evalCoor.getSecond() -  1] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance, evalCoor.getFirst(), evalCoor.getSecond() - 1);
	            status[evalCoor.getFirst()][evalCoor.getSecond() -  1] = DISCOVERED;
	        }
	        // add right
	        if(evalCoor.getFirst() + 1 < width && status[evalCoor.getFirst() + 1][evalCoor.getSecond()] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance, evalCoor.getFirst() + 1, evalCoor.getSecond());
	            status[evalCoor.getFirst() + 1][evalCoor.getSecond()] = DISCOVERED;
	        }
	        // add left
	        if(evalCoor.getFirst() - 1 >= 0 && status[evalCoor.getFirst() - 1][evalCoor.getSecond()] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance, evalCoor.getFirst() -  1, evalCoor.getSecond());
	            status[evalCoor.getFirst() - 1][evalCoor.getSecond()] = DISCOVERED;
	        }
	        // add right-up
	        if((evalCoor.getFirst() + 1 < width && evalCoor.getSecond() - 1 >= 0) && status[evalCoor.getFirst() + 1][evalCoor.getSecond() -  1] != EVALUATED) {
	        	toEvaluate.add(accumulatedCellCost + costDistance*Math.sqrt(2), evalCoor.getFirst() + 1, evalCoor.getSecond() - 1);
	            status[evalCoor.getFirst() + 1][evalCoor.getSecond() -  1] = DISCOVERED;
	        }
	        // add left-up
	        if((evalCoor.getFirst() - 1 >= 0 && evalCoor.getSecond() - 1 >= 0)&& status[evalCoor.getFirst() - 1][evalCoor.getSecond() -  1] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance*Math.sqrt(2), evalCoor.getFirst() - 1, evalCoor.getSecond() - 1);
	            status[evalCoor.getFirst() - 1][evalCoor.getSecond() -  1] = DISCOVERED;
	        }
	        //add right-down
	        if((evalCoor.getFirst() + 1 < width && evalCoor.getSecond() + 1 < height) && status[evalCoor.getFirst() + 1][evalCoor.getSecond() + 1] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance*Math.sqrt(2), evalCoor.getFirst() + 1, evalCoor.getSecond() + 1);
	            status[evalCoor.getFirst() + 1][evalCoor.getSecond() + 1] = DISCOVERED;
	        }
	        //add left-down
	        if((evalCoor.getFirst() - 1 >= 0 && evalCoor.getSecond() + 1 < height) && status[evalCoor.getFirst() - 1][evalCoor.getSecond() + 1] != EVALUATED) {
	            toEvaluate.add(accumulatedCellCost + costDistance*Math.sqrt(2), evalCoor.getFirst() -  1, evalCoor.getSecond() + 1);
	            status[evalCoor.getFirst() - 1][evalCoor.getSecond() + 1] = DISCOVERED;
	        }
	    }
	    
	    // Return generated accumulatedCost map
	    return accumulatedCost;
	}

}
