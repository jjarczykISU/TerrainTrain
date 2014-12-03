package UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import algorithm.MapAnalysis;
import algorithm.MapUtil.Pair;
import fileUtils.FileUtil;

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
	
	private static JLabel altitudeMap;
	private static JLabel waterMap;
	private static JLabel discreteMap;
	private static JLabel accumulatedMap;
	private static JLabel pathMap;
	
	private static MapAnalysis analysis;
	
	private static int[][] altitudeLayer;
	private static int[][] waterLayer;
	
	private static class TrainTerrainPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public TrainTerrainPanel() {
			super(new BorderLayout());
			
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
			add(tabbedPane, BorderLayout.CENTER);
			
			JPanel altitudePanel = new JPanel();
			altitudeMap = new JLabel();
			altitudePanel.add(altitudeMap);
			tabbedPane.addTab("Altitude Map", null, altitudePanel, null);
			
			JPanel waterPanel = new JPanel();
			waterMap = new JLabel();
			waterPanel.add(waterMap);
			tabbedPane.addTab("Water Map", null, waterPanel, null);
			
			JPanel discretePanel = new JPanel();
			discreteMap = new JLabel();
			discretePanel.add(discreteMap);
			tabbedPane.addTab("Discrete Map", null, discretePanel, null);
			
			JPanel accumulatedPanel = new JPanel();
			accumulatedMap = new JLabel();
			accumulatedPanel.add(accumulatedMap);
			tabbedPane.addTab("Accumulated Cost Map", null, accumulatedPanel, null);
			
			JPanel pathPanel = new JPanel();
			pathMap = new JLabel();
			pathPanel.add(pathMap);
			tabbedPane.addTab("Calculated Path", null, pathPanel, null);
			
			
			JPanel inputAndOptionsPanel = new JPanel();
			add(inputAndOptionsPanel, BorderLayout.PAGE_END);
			
			JPanel weightingPanel = new JPanel();
			inputAndOptionsPanel.add(weightingPanel);
			
			
			
			JPanel buttonPanel =  new JPanel();
			inputAndOptionsPanel.add(buttonPanel);
			
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
			
			final JButton altitudeMapButton = new JButton("Altitude Map");
			buttonPanel.add(altitudeMapButton);
			
			altitudeMapButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

					// If file was chosen, update contents of htmlPane
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							altitudeLayer = FileUtil.imageToMap(fileChooser.getSelectedFile());
							altitudeMap.setIcon(new ImageIcon(FileUtil.mapToImage(altitudeLayer)));
						} catch (IOException e) {
							altitudeLayer = null;
							altitudeMap.setIcon(new ImageIcon());
						}						
					}

				}
			});
			
			final JButton waterMapButton = new JButton("Water Map");
			buttonPanel.add(waterMapButton);
			
			waterMapButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

					// If file was chosen, update contents of htmlPane
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							waterLayer = FileUtil.imageToMap(fileChooser.getSelectedFile());
							waterMap.setIcon(new ImageIcon(FileUtil.mapToImage(waterLayer)));
						} catch (IOException e) {
							waterLayer = null;
							waterMap.setIcon(new ImageIcon());
						}						
					}

				}
			});
			
			
			final JButton analysisButton = new JButton("Perform Analysis");
			buttonPanel.add(analysisButton);
			
			analysisButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(altitudeLayer == null) { // If an altitude map has not been set
						JOptionPane.showMessageDialog(null, "Altitude Map is not Set");
					} else if(waterLayer != null && (altitudeLayer.length != waterLayer.length || altitudeLayer[0].length != waterLayer[0].length)) {
						JOptionPane.showMessageDialog(null, "Altitude and Water Map dimensions do not match");
					} else {
						
						List<int[][]> layers = new ArrayList<int[][]>();
						layers.add(invertGraph(altitudeLayer));
						if(waterLayer == null) {
							int[][] defaultWater = new int[altitudeLayer.length][altitudeLayer[0].length];
							layers.add(defaultWater);
						} else{
							layers.add(invertGraph(waterLayer));
						}
						
						// Set source
						int[][] source = new int[altitudeLayer.length][altitudeLayer[0].length];
						source[source.length - 1][source[0].length - 1] = 1;
						// Set start
						Pair<Integer, Integer> start = new Pair<Integer, Integer>(0, 0);
						
						// Perform analysis
						analysis = new MapAnalysis(source, layers, start);
						
						// Update Images
						discreteMap.setIcon(new ImageIcon(discreteCostMapToBufferedImage(analysis.discreteCost)));
						accumulatedMap.setIcon(new ImageIcon(accumulatedCostMapToBufferedImage(analysis.accumulatedCost)));
						pathMap.setIcon(new ImageIcon(pathAndAltitudeToBufferedImage(analysis.path, altitudeLayer)));
					}
				}
			});
			
			final JButton resetButton = new JButton("Reset");
			buttonPanel.add(resetButton);
			
			resetButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// Clear altitudeLayer
					altitudeLayer = null;
					// Clear waterLayer
					waterLayer = null;
					
					// Clear Images
					altitudeMap.setIcon(new ImageIcon());
					waterMap.setIcon(new ImageIcon());
					discreteMap.setIcon(new ImageIcon());
					accumulatedMap.setIcon(new ImageIcon());
					pathMap.setIcon(new ImageIcon());
				}
			});
	
		}
		
		private int[][] invertGraph(int[][] graph) {
			int[][] inverted = new int[graph.length][graph[0].length];
			for(int i = 0; i < graph.length; i++) {
				for(int j = 0; j < graph[0].length; j++) {
					inverted[i][j] = 255 - graph[i][j]; 
				}
			}
			return inverted;
		}
		
		private BufferedImage discreteCostMapToBufferedImage(double[][] discreteCost) {
			int[][] modifiedMap = new int[discreteCost.length][discreteCost[0].length];
			double max = 1;
			for(int i = 0; i < discreteCost.length; i++) {
				for(int j = 0; j < discreteCost[0].length; j++) {
					if(discreteCost[i][j] > max) max = discreteCost[i][j]; 
				}
			}
			for(int i = 0; i < discreteCost.length; i++) {
				for(int j = 0; j < discreteCost[0].length; j++) {
					modifiedMap[i][j] = (int)(discreteCost[i][j] * 255/max);
				}
			}
			return (BufferedImage) FileUtil.mapToImage(modifiedMap);
		}
		
		private BufferedImage accumulatedCostMapToBufferedImage(double[][] accumulatedCost) {
			int[][] modifiedMap = new int[accumulatedCost.length][accumulatedCost[0].length];
			double max = 1;
			for(int i = 0; i < accumulatedCost.length; i++) {
				for(int j = 0; j < accumulatedCost[0].length; j++) {
					if(accumulatedCost[i][j] > max) max = accumulatedCost[i][j]; 
				}
			}
			for(int i = 0; i < modifiedMap.length; i++) {
				for(int j = 0; j < modifiedMap[0].length; j++) {
					modifiedMap[i][j] = (int)(accumulatedCost[i][j]*255/max);
				}
			}
			return (BufferedImage) FileUtil.mapToImage(modifiedMap);
		}
		
		private BufferedImage pathAndAltitudeToBufferedImage(int[][] path, int[][] altitudeMap) {
			BufferedImage pathImage = FileUtil.mapToImage(altitudeMap);
			for(int i = 0; i < path.length; i++) {
				for(int j = 0; j < path[0].length; j++) {
					if(path[i][j] == 1) {
						pathImage.setRGB(i, j, 255<<16); //red
					}
				}
			}
			return pathImage;
		}
	}
}
