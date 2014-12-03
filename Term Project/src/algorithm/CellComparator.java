package algorithm;

import java.util.Comparator;

import algorithm.MapUtil.Pair;

public class CellComparator implements Comparator<Pair<Double, Pair<Integer,Integer>>> {

	@Override
	public int compare(Pair<Double, Pair<Integer, Integer>> arg0,
			Pair<Double, Pair<Integer, Integer>> arg1) {
		
		return (int) Math.copySign(1, (arg0.getFirst() - arg1.getFirst()));
	}

}
