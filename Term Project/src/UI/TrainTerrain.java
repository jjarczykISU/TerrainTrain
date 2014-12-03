package UI;

import java.awt.Dimension;

import javax.swing.JFrame;

public class TrainTerrain {
	/**
	 * Opens a GUI that allows for users to input images representing geographical information and generates an optimal path.
	 * @param args
	 */
	public static void main(String[] args) {
		// Create a JFrame that holds the TrainTerrainPanel
		JFrame frame = new JFrame("Terrain Train");
		
		// Set default close operation to exit the program on close
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set intial dimensions of window
		Dimension minSize = new Dimension(500, 500);
		frame.setSize(minSize);
		frame.setMinimumSize(minSize);
		frame.pack();

		// Set content of TrainTerrain
		frame.setContentPane(new TrainTerrainPanel());
		
		// Make frame visible
		frame.setVisible(true);
	}
}
