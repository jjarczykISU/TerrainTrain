package UI;

import java.awt.Dimension;

import javax.swing.JFrame;

public class TrainTerrain {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Terrain Train");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dimension minSize = new Dimension(500, 500);
		frame.setSize(minSize);
		frame.setMinimumSize(minSize);
		frame.pack();

		frame.setContentPane(new TrainTerrainPanel());
		frame.setVisible(true);
	}
}
