package UI;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import algorithm.MapAnalysis;
import algorithm.MapUtil;
import algorithm.MapUtil.Pair;
import fileUtils.FileUtil;

public class TrainTerrainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	//TODO Clean up code
	
	private JTabbedPane tabbedPane;
	
	private JLabel altitudeMap;
	private JLabel waterMap;
	private JLabel discreteMap;
	private JLabel accumulatedMap;
	private JLabel pathMap;
	
	private BufferedImage altitudeImage, waterImage, discreteImage, accumulatedImage, pathImage;

	private MapAnalysis analysis;

	private double[][] altitudeLayer;
	private double[][] waterLayer;
	
	// TODO add ui input	
	// Dimensions of a cell in meters
	private double cellSize = 1.0;
	
	// TODO add ui input
	// Scale of map altitudes
	// Calculated by: (max - min)/255.0, where max is the altitude of a black cell in meters and min is the altitude of a white cell in meters
	private double altitudeScale = 1.0;

	public TrainTerrainPanel() {
		// Set layout of panel to BorderLayout 
		super(new BorderLayout());
		
		// Add a JTabbedPane
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		add(tabbedPane, BorderLayout.CENTER);
		
		// Add Panels to JTabbedPane that will hold the image data
		JPanel altitudePanel = new JPanel(new BorderLayout());
		altitudeMap = new JLabel();
		altitudeMap.setHorizontalAlignment(JLabel.CENTER);
		altitudeMap.setVerticalAlignment(JLabel.CENTER);
		altitudePanel.add(altitudeMap);
		tabbedPane.addTab("Altitude Map", null, altitudePanel, null);
		
		JPanel waterPanel = new JPanel(new BorderLayout());
		waterMap = new JLabel();
		waterMap.setHorizontalAlignment(JLabel.CENTER);
		waterMap.setVerticalAlignment(JLabel.CENTER);
		waterPanel.add(waterMap);
		tabbedPane.addTab("Water Map", null, waterPanel, null);
		
		JPanel discretePanel = new JPanel(new BorderLayout());
		discreteMap = new JLabel();
		discreteMap.setHorizontalAlignment(JLabel.CENTER);
		discreteMap.setVerticalAlignment(JLabel.CENTER);
		discretePanel.add(discreteMap);
		tabbedPane.addTab("Discrete Map", null, discretePanel, null);
		
		JPanel accumulatedPanel = new JPanel(new BorderLayout());
		accumulatedMap = new JLabel();
		accumulatedMap.setHorizontalAlignment(JLabel.CENTER);
		accumulatedMap.setVerticalAlignment(JLabel.CENTER);
		accumulatedPanel.add(accumulatedMap);
		tabbedPane.addTab("Accumulated Cost Map", null, accumulatedPanel, null);
		
		JPanel pathPanel = new JPanel(new BorderLayout());
		pathMap = new JLabel();
		pathMap.setHorizontalAlignment(JLabel.CENTER);
		pathMap.setVerticalAlignment(JLabel.CENTER);
		pathPanel.add(pathMap);
		tabbedPane.addTab("Calculated Path", null, pathPanel, null);
		
		
		// Add Buttons for User Interaction
		JPanel inputAndOptionsPanel = new JPanel();
		add(inputAndOptionsPanel, BorderLayout.PAGE_END);
		
		JPanel weightingPanel = new JPanel();
		inputAndOptionsPanel.add(weightingPanel);
		
		JPanel buttonPanel =  new JPanel();
		inputAndOptionsPanel.add(buttonPanel);
		
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
		
		// Add buttons
		final JButton altitudeMapButton = new JButton("Load Altitude Map");
		buttonPanel.add(altitudeMapButton);
		final JButton waterMapButton = new JButton("Load Water Map");
		buttonPanel.add(waterMapButton);
		final JButton waterLevelButton = new JButton("Set Water Level");
		buttonPanel.add(waterLevelButton);
		final JButton analysisButton = new JButton("Perform Analysis");
		buttonPanel.add(analysisButton);
		final JButton resetButton = new JButton("Reset");
		buttonPanel.add(resetButton);
		
		// Add ActionListeners for buttons
		altitudeMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
					try {
						altitudeImage = ImageIO.read(fileChooser.getSelectedFile());
						altitudeLayer = invertGraph(FileUtil.imageToMap(altitudeImage)); // Interpreting white as low altitude and black as high altitude
					} catch (IOException e) {
						altitudeImage = null;
						altitudeLayer = null;
					}
					updateImages();					
				}

			}
		});
		waterMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
					try {
						waterImage = ImageIO.read(fileChooser.getSelectedFile());
						waterLayer = invertGraph(FileUtil.imageToMap(waterImage)); // Interpreting white as no water and black as the deepest water
					} catch (IOException e) {
						waterImage = null;
						waterLayer = null;
					}
					updateImages();
				}

			}
		});
		waterLevelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(altitudeLayer == null) { // If an altitude map has not been set
					JOptionPane.showMessageDialog(null, "Altitude Map is not Set");
					return;
				}
				
				//query user for water level
				String input = JOptionPane.showInputDialog("Altitude of water level?");
				double level;
				if (input != null) try {
					level = Double.valueOf(input);
				} catch (NumberFormatException e) {
					//bad input
					return; //ignore
				} else {
					return; //cancelled
				}
				
				//generate water map
				if (level > 0) {
					//generate water map
					int width = altitudeLayer.length;
					int height = altitudeLayer[0].length;
					waterLayer = new double[width][height];
					for(int i = 0; i < width; i++) {
						for(int j = 0; j < height; j ++) {
							double altitude = altitudeLayer[i][j]*cellSize;
							if (altitude < level) {
								waterLayer[i][j] = level - altitude;
							} else {
								waterLayer[i][j] = 0;
							}
							
						}
					}
					//create image
					waterImage = FileUtil.mapToImage(invertGraph(waterLayer));
				} else {
					waterImage = null;
					waterLayer = null;
				}
				updateImages();
			}
		});
		analysisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(altitudeLayer == null) { // If an altitude map has not been set
					JOptionPane.showMessageDialog(null, "Altitude Map is not Set");
				} else if(waterLayer != null && (altitudeLayer.length != waterLayer.length || altitudeLayer[0].length != waterLayer[0].length)) {
					JOptionPane.showMessageDialog(null, "Altitude and Water Map dimensions do not match");
				} else {
					// Set mapping of map type to map data
					Map<MapUtil.MapTypes, double[][]> layers = new HashMap<MapUtil.MapTypes, double[][]>();
					layers.put(MapUtil.MapTypes.ALTITUDE, altitudeLayer);
					if(waterLayer != null) {
						layers.put(MapUtil.MapTypes.WATER, waterLayer);
					}
					
					// Set source for path
					int[][] source = new int[altitudeLayer.length][altitudeLayer[0].length];
					source[source.length - 1][source[0].length - 1] = 1;
					// Set start for path
					Pair<Integer, Integer> start = new Pair<Integer, Integer>(0, 0);
					
					//TODO modify this to be updated in the UI
					Map<MapUtil.MapTypes, Double> weightings = new HashMap<MapUtil.MapTypes, Double>();
					weightings.put(MapUtil.MapTypes.ALTITUDE, 1.0);
					weightings.put(MapUtil.MapTypes.WATER, 5.0);
					
					// TODO remove if this check is handled in the UI with default values
					for(MapUtil.MapTypes layer : layers.keySet()) {
						if(!weightings.containsKey(layer)) weightings.put(layer, 1.0);
					}
					
					//TOD add UI control for this
					double costDistance = 1.0;
					
					// Perform analysis
					analysis = new MapAnalysis(source, start, layers, cellSize, altitudeScale, costDistance, weightings);
					
					// Update Images
					discreteImage = mapToBufferedImage(analysis.discreteCost);
					accumulatedImage = mapToBufferedImage(analysis.accumulatedCost);
					pathImage = pathAndAltitudeToBufferedImage(analysis.path, altitudeLayer);
					updateImages();
				}
			}
		});		
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Clear altitudeLayer
				altitudeLayer = null;
				// Clear waterLayer
				waterLayer = null;
				
				// Clear Images
				altitudeImage = waterImage = discreteImage = accumulatedImage = pathImage = null;
				updateImages();
			}
		});
		//Add listener for window resizing
		tabbedPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();
			}

			@Override
			public void componentHidden(ComponentEvent arg0) { /*nothing*/ }

			@Override
			public void componentMoved(ComponentEvent arg0) { /*nothing*/ }

			@Override
			public void componentShown(ComponentEvent arg0) { /*nothing*/ }
		});

	}
	
	/**
	 * Inverts the colors of an image graph.
	 * @param graph 2D array to be inverted
	 * @return a graph where all the values are inverted using value = 255 - value for each cell
	 */
	private double[][] invertGraph(double[][] graph) {
		double[][] inverted = new double[graph.length][graph[0].length];
		for(int i = 0; i < graph.length; i++) {
			for(int j = 0; j < graph[0].length; j++) {
				inverted[i][j] = 255 - graph[i][j]; 
			}
		}
		return inverted;
	}
	/**
	 * Clears all the images that are generated from map analysis
	 */
	private void clearAnalysisImages() {
		discreteImage = accumulatedImage = pathImage = null;
	}
	
	/**
	 * updates displayed images, scaled to fit window
	 */
	private void updateImages() {
		//find size of our images, if possible
		int imageWidth, imageHeight;
		if (altitudeImage != null) {
			imageWidth = altitudeImage.getWidth();
			imageHeight = altitudeImage.getHeight();
		} else if (waterImage != null) {
			imageWidth = waterImage.getWidth();
			imageHeight = waterImage.getHeight();
		} else {
			altitudeMap.setIcon(null);
			waterMap.setIcon(null);
			discreteMap.setIcon(null);
			accumulatedMap.setIcon(null);
			pathMap.setIcon(null);
			return;
		}
		int paneWidth = altitudeMap.getWidth();
		int paneHeight = altitudeMap.getHeight();
		//calculate desired display dimensions
		int width, height;
		if ((double)paneWidth/paneHeight < (double)imageWidth/imageHeight) { //comparing aspect ratios (must be floating point)
			//match pane width
			width = paneWidth;
			height = paneWidth * imageHeight / imageWidth;
		} else {
			//match pane height
			width = paneHeight * imageWidth / imageHeight;
			height = paneHeight;
		}
		
		updateImage(altitudeMap, altitudeImage, width, height);
		updateImage(waterMap, waterImage, width, height);
		updateImage(discreteMap, discreteImage, width, height);
		updateImage(accumulatedMap, accumulatedImage, width, height);
		updateImage(pathMap, pathImage, width, height);
	}
	//a helper function
	private void updateImage(JLabel label, BufferedImage image, int width, int height) {
		if (image != null) {
			label.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_DEFAULT)));
		} else {
			label.setIcon(null);
		}
	}
	
	/**
	 * Converts 2D array to a grayscale BufferedImage
	 * @param dataMap 2D array to be converted to BufferedImage
	 * @return converted BufferedImage
	 */
	private BufferedImage mapToBufferedImage(double[][] dataMap) {
		double[][] modifiedMap = new double[dataMap.length][dataMap[0].length];
		double max = 1;
		for(int i = 0; i < dataMap.length; i++) {
			for(int j = 0; j < dataMap[0].length; j++) {
				if(dataMap[i][j] > max) max = dataMap[i][j]; 
			}
		}
		for(int i = 0; i < dataMap.length; i++) {
			for(int j = 0; j < dataMap[0].length; j++) {
				modifiedMap[i][j] = (int)(dataMap[i][j] * 255/max);
			}
		}
		return (BufferedImage) FileUtil.mapToImage(modifiedMap);
	}

	/**
	 * Converts path and altitudeMap to a BUfferedImage that will show the path as a red line on the altitudeMap
	 * @param path 2D path array that represents a path that will be overlayed on the image
	 * @param altitudeMap 2D altitudeMap array that represents the altitude data
	 * @return converted BufferedImage
	 */
	private BufferedImage pathAndAltitudeToBufferedImage(int[][] path, double[][] altitudeMap) {
		BufferedImage pathImage = FileUtil.mapToImage(invertGraph(altitudeMap));
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
