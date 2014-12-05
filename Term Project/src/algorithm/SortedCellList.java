package algorithm;

import java.util.PriorityQueue;

import algorithm.MapUtil.Pair;

public class SortedCellList {
	PriorityQueue<Pair<Double, Pair<Integer, Integer>>> list;

	public SortedCellList() {
		list = new PriorityQueue<Pair<Double, Pair<Integer, Integer>>>(10, new CellComparator());
	}

	// / Adds cell to queue
	public void add(double value, int xLoc, int yLoc) {
		add(value, new Pair<Integer, Integer>(xLoc, yLoc));
	}

	public void add(double value, Pair<Integer, Integer> loc) {
		Pair<Double, Pair<Integer, Integer>> toAdd = new Pair<Double, Pair<Integer, Integer>>(value, loc);
		list.add(toAdd);
	}

	public Pair<Double, Pair<Integer, Integer>> removeFirst() {
		if (list.isEmpty())
			return null;
		return list.remove();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}
}
