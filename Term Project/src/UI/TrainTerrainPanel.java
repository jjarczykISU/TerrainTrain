package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
	private JLabel roadsMap;
	private JLabel housingMap;
	private JLabel discreteMap;
	private JLabel accumulatedMap;
	private JLabel pathMap;
	
	JCheckBox doSave;
	
	private BufferedImage altitudeImage, waterImage, roadsImage, housingImage, discreteImage, accumulatedImage, pathImage;

	private MapAnalysis analysis;

	private double[][] altitudeLayer;
	private double[][] waterLayer;
	private double[][] roadsLayer;
	private double[][] housingLayer;
	
	// Dimensions of a cell in meters
	private double cellSize;
	
	// Scale of map altitudes
	// Calculated by: (max - min)/255.0, where max is the altitude of a black cell in meters and min is the altitude of a white cell in meters
	private double altitudeScale;
	private double min = 0.0;
	private double max = 255.0;
	
	// Weightings for each map type
	Map<MapUtil.MapTypes, Double> weightings;

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
		
		JPanel roadsPanel = new JPanel(new BorderLayout());
		roadsMap = new JLabel();
		roadsMap.setHorizontalAlignment(JLabel.CENTER);
		roadsMap.setVerticalAlignment(JLabel.CENTER);
		roadsPanel.add(roadsMap);
		tabbedPane.addTab("Roads Map", null, roadsPanel, null);
		
		JPanel housingPanel = new JPanel(new BorderLayout());
		housingMap = new JLabel();
		housingMap.setHorizontalAlignment(JLabel.CENTER);
		housingMap.setVerticalAlignment(JLabel.CENTER);
		housingPanel.add(housingMap);
		tabbedPane.addTab("Housing Density Map", null, housingPanel, null);
		
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
		inputAndOptionsPanel.setLayout(new BoxLayout(inputAndOptionsPanel, BoxLayout.Y_AXIS));
		add(inputAndOptionsPanel, BorderLayout.PAGE_END);
		
		JPanel constantsPanel = new JPanel();
		inputAndOptionsPanel.add(constantsPanel);
		
		
		JPanel weightingsPanel = new JPanel();
		weightingsPanel.setLayout(new BoxLayout(weightingsPanel, BoxLayout.Y_AXIS));
		constantsPanel.add(weightingsPanel);
		
		// Initialize weightings and add input to ui
		weightings = new HashMap<MapUtil.MapTypes, Double>();
		for(final MapUtil.MapTypes type : MapUtil.MapTypes.values()) {
			weightings.put(type, 1.0);
			
			JPanel weightPanel = new JPanel();
			weightPanel.add(new JLabel(type.name()));
			final JTextField weightEntry = new JTextField();
			weightEntry.setText("1.0      ");
			weightEntry.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    warn();
			  }

			  public void warn() {
				  try {
			    		double parsed =  Double.parseDouble(weightEntry.getText());
			    		if(parsed >= 0) {
			    			weightings.put(type, parsed);
						} else {
							JOptionPane.showMessageDialog(null,
							          "Error: Please enter a double bigger than 0", "Error Massage",
							          JOptionPane.ERROR_MESSAGE);
						}
			    	} catch(Exception ex) {
			    		JOptionPane.showMessageDialog(null,
						          "Error: Please enter a double bigger than 0", "Error Massage",
						          JOptionPane.ERROR_MESSAGE);
			    	}
			  	}
			});
			weightPanel.add(weightEntry);
			
			weightingsPanel.add(weightPanel);
		}

		
		cellSize = 1.0;
		
		JPanel cellSizePanel = new JPanel();
		cellSizePanel.add(new JLabel("Pixel Size (meters)"));
		final JTextField cellSizeEntry = new JTextField();
		cellSizeEntry.setText("1.0      ");
		cellSizeEntry.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
		    warn();
		  }
		  public void removeUpdate(DocumentEvent e) {
		    warn();
		  }
		  public void insertUpdate(DocumentEvent e) {
		    warn();
		  }

		  public void warn() {
			  try {
		    		double parsed =  Double.parseDouble(cellSizeEntry.getText());
		    		if(parsed > 0) {
		    			cellSize = parsed;
					}
		    	} catch(Exception ex) {
		    		JOptionPane.showMessageDialog(null,
					          "Error: Please enter a double bigger than 0", "Error Massage",
					          JOptionPane.ERROR_MESSAGE);
		    	}
		  	}
		});
		cellSizePanel.add(cellSizeEntry);
		
		constantsPanel.add(cellSizePanel);
		
		// Calculated by: (max - min)/255.0, where max is the altitude of a black cell in meters and min is the altitude of a white cell in meters
		altitudeScale = 1.0;
		
		JPanel altitudeScalePanel = new JPanel();
		altitudeScalePanel.setLayout(new BoxLayout(altitudeScalePanel, BoxLayout.Y_AXIS));
		JPanel minPanel = new JPanel();
		altitudeScalePanel.add(minPanel);
		minPanel.add(new JLabel("Low (black) in meters:"));
		JPanel maxPanel = new JPanel();
		altitudeScalePanel.add(maxPanel);
		maxPanel.add(new JLabel("High (white) in meters:"));
		
		final JTextField minEntry = new JTextField();
		minEntry.setText("0.0              ");
		minEntry.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
		    warn();
		  }
		  public void removeUpdate(DocumentEvent e) {
		    warn();
		  }
		  public void insertUpdate(DocumentEvent e) {
		    warn();
		  }

		  public void warn() {
			  try {
		    		double parsed =  Double.parseDouble(minEntry.getText());
		    		min = parsed;
		    		if(max > min && min >= 0.0) {
		    			altitudeScale = (max - min)/255;
					} else {
						JOptionPane.showMessageDialog(null,
						          "Error: Please enter a positive double smaller than max", "Error Massage",
						          JOptionPane.ERROR_MESSAGE);
					}
		    	} catch(Exception ex) {
		    		JOptionPane.showMessageDialog(null,
					          "Error: Please enter a double", "Error Massage",
					          JOptionPane.ERROR_MESSAGE);
		    	}
		  	}
		});
		minPanel.add(minEntry);
		final JTextField maxEntry = new JTextField();
		maxEntry.setText("255.0         ");
		maxEntry.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
		    warn();
		  }
		  public void removeUpdate(DocumentEvent e) {
		    warn();
		  }
		  public void insertUpdate(DocumentEvent e) {
		    warn();
		  }

		  public void warn() {
			  try {
		    		double parsed =  Double.parseDouble(maxEntry.getText());
		    		max = parsed;
		    		if(max > min && min >= 0.0) {
		    			altitudeScale = (max - min)/255;
					} else {
						JOptionPane.showMessageDialog(null,
						          "Error: Please enter a double bigger than min", "Error Massage",
						          JOptionPane.ERROR_MESSAGE);
					}
		    	} catch(Exception ex) {
		    		JOptionPane.showMessageDialog(null,
					          "Error: Please enter a double", "Error Massage",
					          JOptionPane.ERROR_MESSAGE);
		    	}
		  	}
		});
		maxPanel.add(maxEntry);
		
		constantsPanel.add(altitudeScalePanel);
		

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
		final JButton roadsMapButton = new JButton("Load Roads Map");
		buttonPanel.add(roadsMapButton);
		final JButton housingMapButton = new JButton("Load Housing Density Map ");
		buttonPanel.add(housingMapButton);
		
		JPanel analysisPanel =  new JPanel();
		inputAndOptionsPanel.add(analysisPanel);
		
		final JButton analysisButton = new JButton("Perform Analysis");
		analysisPanel.add(analysisButton);
		final JButton resetButton = new JButton("Reset");
		analysisPanel.add(resetButton);
		
		// Add checkbox
		JPanel doSavePanel = new JPanel();
		analysisPanel.add(doSavePanel);
		JLabel doSaveLabel = new JLabel("save to file:");
		doSave = new JCheckBox();
		doSavePanel.add(doSaveLabel);
		doSavePanel.add(doSave);
		
		
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
						altitudeLayer = FileUtil.imageToMap(altitudeImage); // Interpreting black as low altitude and white as high altitude
					} catch (IOException e) {
						altitudeImage = null;
						altitudeLayer = null;
					}
					updateImages();	
					// Change selected tab
					tabbedPane.setSelectedIndex(0);
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
						waterLayer = FileUtil.imageToMap(waterImage); // Interpreting black as no water and white as the deepest water
					} catch (IOException e) {
						waterImage = null;
						waterLayer = null;
					}
					updateImages();
					// Change selected tab
					tabbedPane.setSelectedIndex(1);
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
					waterImage = FileUtil.mapToImage(waterLayer);
				} else {
					waterImage = null;
					waterLayer = null;
				}
				updateImages();
				// Change selected tab
				tabbedPane.setSelectedIndex(1);
			}
		});
		roadsMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
					try {
						roadsImage = ImageIO.read(fileChooser.getSelectedFile());
						roadsLayer = invertGraph(FileUtil.imageToMap(roadsImage)); // Interpreting white as no road, otherwise road
					} catch (IOException e) {
						roadsImage = null;
						roadsLayer = null;
					}
					updateImages();
					// Change selected tab
					tabbedPane.setSelectedIndex(2);
				}

			}
		});
		housingMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
					try {
						housingImage = ImageIO.read(fileChooser.getSelectedFile());
						housingLayer = invertGraph(FileUtil.imageToMap(housingImage)); // Interpreting white as no houses and darker as housing density increases
					} catch (IOException e) {
						housingImage = null;
						housingLayer = null;
					}
					updateImages();
					// Change selected tab
					tabbedPane.setSelectedIndex(3);
				}

			}
		});
		analysisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(altitudeLayer == null) { // If an altitude map has not been set
					JOptionPane.showMessageDialog(null, "Altitude Map is not Set");
				} else if(waterLayer != null && (altitudeLayer.length != waterLayer.length || altitudeLayer[0].length != waterLayer[0].length)) {
					JOptionPane.showMessageDialog(null, "Altitude and Water Map dimensions do not match");
				} else if(roadsLayer != null && (altitudeLayer.length != roadsLayer.length || altitudeLayer[0].length != roadsLayer[0].length)) {
					JOptionPane.showMessageDialog(null, "Altitude and roads Map dimensions do not match");
				} else if(housingLayer != null && (altitudeLayer.length != housingLayer.length || altitudeLayer[0].length != housingLayer[0].length)) {
					JOptionPane.showMessageDialog(null, "Altitude and housing density Map dimensions do not match");
				} else {
					// Set mapping of map type to map data
					Map<MapUtil.MapTypes, double[][]> layers = new HashMap<MapUtil.MapTypes, double[][]>();
					layers.put(MapUtil.MapTypes.ALTITUDE, altitudeLayer);
					if(waterLayer != null) layers.put(MapUtil.MapTypes.WATER, waterLayer);
					if(roadsLayer != null) layers.put(MapUtil.MapTypes.ROADS, roadsLayer);
					if(housingLayer != null) layers.put(MapUtil.MapTypes.HOUSINGDENSITY, housingLayer);
					
					// Set source for path
					int[][] source = new int[altitudeLayer.length][altitudeLayer[0].length];
					source[source.length - 1][source[0].length - 1] = 1;
					// Set start for path
					Pair<Integer, Integer> start = new Pair<Integer, Integer>(0, 0);
					
					//TOD add UI control for this
					double costDistance = 1.0;
					
					// Perform analysis
					analysis = new MapAnalysis(source, start, layers, cellSize, altitudeScale, costDistance, weightings);
					
					// Update Images
					discreteImage = mapToBufferedImageColor(analysis.discreteCost);
					accumulatedImage = mapToBufferedImageColor(analysis.accumulatedCost);
					pathImage = pathAndAltitudeToBufferedImage(analysis.path, altitudeLayer);
					updateImages();
					
					// Save analysis data if doSave is checked
					if(doSave.isSelected()) {
						HashMap<String, BufferedImage> analysisData = new HashMap<String, BufferedImage>();
						analysisData.put("discreteMap", discreteImage);
						analysisData.put("accumulatedMap", accumulatedImage);
						analysisData.put("pathMap", pathImage);
						// Add input files to be saved as well
						analysisData.put("altitudeMap", altitudeImage);
						if(waterLayer != null) analysisData.put("waterMap", waterImage);
						if(roadsLayer != null) analysisData.put("waterMap", roadsImage);
						if(housingLayer != null) analysisData.put("waterMap", housingImage);
						saveAnalysisData(analysisData);
					}
					// Change selected tab to path tab
					tabbedPane.setSelectedIndex(6);
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
				// Clear roadsLayer
				roadsLayer = null;
				// Clear housingLayer
				housingLayer = null;
				
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
		} else if (roadsImage != null) {
			imageWidth = roadsImage.getWidth();
			imageHeight = roadsImage.getHeight();
		} else if (housingImage != null) {
			imageWidth = housingImage.getWidth();
			imageHeight = housingImage.getHeight();
		} else {
			altitudeMap.setIcon(null);
			waterMap.setIcon(null);
			roadsMap.setIcon(null);
			housingMap.setIcon(null);
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
		updateImage(roadsMap, roadsImage, width, height);
		updateImage(housingMap, housingImage, width, height);
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
	 * Converts 2D array to a color BufferedImage where high values are red, medium are yellow and low are green
	 * @param dataMap 2D array to be converted to BufferedImage
	 * @return converted BufferedImage
	 */
	private BufferedImage mapToBufferedImageColor(double[][] dataMap) {
		BufferedImage colorImage = FileUtil.mapToImage(invertGraph(dataMap));
		double max = 1;
		for(int i = 0; i < dataMap.length; i++) {
			for(int j = 0; j < dataMap[0].length; j++) {
				if(dataMap[i][j] > max) max = dataMap[i][j]; 
			}
		}
		for(int i = 0; i < dataMap.length; i++) {
			for(int j = 0; j < dataMap[0].length; j++) {
				int scaledValue = (int)(dataMap[i][j]*altitudeScale*255/max);
				Color color = new Color(Math.min(255, (int)(255*((scaledValue)/127.0))), Math.min(255, 255 - (int)(255*((scaledValue - 128)/128.0))), 0);
				colorImage.setRGB(i, j, color.getRGB());
			}
		}
		return colorImage;
	}

	/**
	 * Converts path and altitudeMap to a BUfferedImage that will show the path as a red line on the altitudeMap
	 * @param path 2D path array that represents a path that will be overlayed on the image
	 * @param altitudeMap 2D altitudeMap array that represents the altitude data
	 * @return converted BufferedImage
	 */
	private BufferedImage pathAndAltitudeToBufferedImage(int[][] path, double[][] altitudeMap) {
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
	
	/**
	 * Saves images from analysis data to separate files inside of a directory names based off of current date and time.
	 * @param mapImages mapping of names to images for analysis data
	 */
	private void saveAnalysisData(Map<String, BufferedImage> mapImages) {
		// Create folder for data using current date/time
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
		String dirName = dateFormat.format(date); // directory name based on date and time
		File dir = new File(dirName);
		try {
			dir.mkdir();
			// Save images to files inside of directory
			for(String mapName : mapImages.keySet()) {
				File mapFile = new File(dir, mapName + ".png");
				mapFile.createNewFile();
				saveImageToFile(mapFile, mapImages.get(mapName));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Writes image to a file.
	 * @param file file to be written to
	 * @param image image to write to file
	 */
	private void saveImageToFile(File file, BufferedImage image) {
		try {
			ImageIO.write(image, "png", file);
		} catch(IOException e) {}
	}
	
	
}
