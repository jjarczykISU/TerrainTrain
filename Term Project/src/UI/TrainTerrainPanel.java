package UI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import algorithm.MapAnalysis;
import algorithm.MapUtil;
import algorithm.MapUtil.Pair;
import fileUtils.FileUtil;

public class TrainTerrainPanel extends JPanel {
	//TODO Clean up code
	
	private JLabel altitudeMap;
	private JLabel waterMap;
	private JLabel discreteMap;
	private JLabel accumulatedMap;
	private JLabel pathMap;

	private MapAnalysis analysis;

	private int[][] altitudeLayer;
	private int[][] waterLayer;
	
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
		
		
		final JButton altitudeMapButton = new JButton("Altitude Map");
		buttonPanel.add(altitudeMapButton);
		
		altitudeMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
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
					clearAnalysisImages();
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
					// Set mapping of map type to map data
					Map<MapUtil.MapTypes, int[][]> layers = new HashMap<MapUtil.MapTypes, int[][]>();
					layers.put(MapUtil.MapTypes.ALTITUDE, invertGraph(altitudeLayer));
					if(waterLayer != null) {
						layers.put(MapUtil.MapTypes.WATER, invertGraph(waterLayer));
					}
					
					// Set source for path
					int[][] source = new int[altitudeLayer.length][altitudeLayer[0].length];
					source[source.length - 1][source[0].length - 1] = 1;
					// Set start for path
					Pair<Integer, Integer> start = new Pair<Integer, Integer>(0, 0);
					
					//TODO modify this to be updated in the UI
					//TODO also make sure that each MapType in layers has a weighting (otherwise an exception is thrown in MapAnalysis)
					Map<MapUtil.MapTypes, Double> weightings = new HashMap<MapUtil.MapTypes, Double>();
					weightings.put(MapUtil.MapTypes.ALTITUDE, 1.0);
					weightings.put(MapUtil.MapTypes.WATER, 2.0);
					
					// Perform analysis
					analysis = new MapAnalysis(source, start, layers, weightings);
					
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
				clearAnalysisImages();
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
		int[][] modifiedMap = new int[altitudeMap.length][altitudeMap[0].length];
		for(int i = 0; i < path.length; i++) {
			for(int j = 0; j < path[0].length; j++) {
				if(path[i][j] == 1) modifiedMap[i][j] = 0;
				else modifiedMap[i][j] = altitudeMap[i][j];
			}
		}
		return (BufferedImage) FileUtil.mapToImage(modifiedMap);
	}
	
	private void clearAnalysisImages() {
		discreteMap.setIcon(new ImageIcon());
		accumulatedMap.setIcon(new ImageIcon());
		pathMap.setIcon(new ImageIcon());
	}
}
