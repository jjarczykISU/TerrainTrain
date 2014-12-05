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
import java.util.HashSet;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
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
	
	private JTabbedPane tabbedPane;
	private OverlayPanel altitudePanel, waterPanel, roadsPanel, housingPanel, discretePanel, accumulatedPanel;
	private BufferedImage altitudeImage, waterImage, roadsImage, housingImage, discreteImage, accumulatedImage;
	private BufferedImage pathOverlay;
	
	HashSet<JTextField> weightingEntries;
	
	JTextField pixelSizeEntry;
	
	JTextField minAltitudeEntry;
	JTextField maxAltitudeEntry;
	
	JCheckBox doSave;
	
	boolean noWarning = false;
	

	private MapAnalysis analysis;

	// Map layers used in analyis
	private double[][] altitudeLayer;
	private double[][] waterLayer;
	private double[][] roadsLayer;
	private double[][] housingLayer;
	
	// Dimensions of a cell in meters
	private double pixelSize;
	
	// Scale of map altitudes
	// altitudeScale alculated by: (max - min)/255.0, where max is the altitude of a black cell in meters and min is the altitude of a white cell in meters
	// temporary values used to store what was interpreted in the ui
	private double tempMin = 0.0;
	private double tempMax = 255.0;
	// mac and min altitude that conforms to the correct constraints where max > min >= 0
	private double minAltitude = 0.0;
	private double maxAltitude = 255.0;
	
	// Water level (for when generating water level)
	double waterLevel = 0;
	
	// Weightings for each map type
	Map<MapUtil.MapTypes, Double> weightings;

	public TrainTerrainPanel() {
		// Set layout of panel to BorderLayout 
		super(new BorderLayout());
		
		// Add a JTabbedPane
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		add(tabbedPane, BorderLayout.CENTER);
		
		// Add Panels to JTabbedPane that will hold the image data
		altitudePanel = new OverlayPanel(new BorderLayout());
		tabbedPane.addTab("Altitude Map", null, altitudePanel, null);
		
		waterPanel = new OverlayPanel(new BorderLayout());
		tabbedPane.addTab("Water Map", null, waterPanel, null);
		
		roadsPanel = new OverlayPanel(new BorderLayout());
		tabbedPane.addTab("Roads Map", null, roadsPanel, null);
		
		housingPanel = new OverlayPanel(new BorderLayout());
		tabbedPane.addTab("Housing Density Map", null, housingPanel, null);
		
		discretePanel = new OverlayPanel(new BorderLayout());
		tabbedPane.addTab("Discrete Map", null, discretePanel, null);
		
		accumulatedPanel = new OverlayPanel(new BorderLayout());
		tabbedPane.addTab("Accumulated Cost Map", null, accumulatedPanel, null);
		
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
		weightingEntries = new HashSet<JTextField>();
		for(final MapUtil.MapTypes type : MapUtil.MapTypes.values()) {
			weightings.put(type, 1.0);
			
			JPanel weightPanel = new JPanel();
			weightPanel.add(new JLabel(type.name()));
			final JTextField weightEntry = new JTextField();
			weightingEntries.add(weightEntry);
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
						} else if(!noWarning) {
							JOptionPane.showMessageDialog(null,
							          "Error: Please enter a double bigger than 0", "Error Message",
							          JOptionPane.ERROR_MESSAGE);
						}
			    	} catch(Exception ex) {
			    		if (!noWarning) JOptionPane.showMessageDialog(null,
						          "Error: Please enter a double bigger than 0", "Error Message",
						          JOptionPane.ERROR_MESSAGE);
			    	}
			  	}
			});
			weightPanel.add(weightEntry);
			
			weightingsPanel.add(weightPanel);
		}

		
		pixelSize = 1.0;
		
		JPanel cellSizePanel = new JPanel();
		cellSizePanel.add(new JLabel("Pixel Size (meters)"));
		final JTextField pixelSizeEntry = new JTextField();
		pixelSizeEntry.setText("1.0      ");
		pixelSizeEntry.getDocument().addDocumentListener(new DocumentListener() {
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
		    		double parsed =  Double.parseDouble(pixelSizeEntry.getText());
		    		if(parsed > 0) {
		    			pixelSize = parsed;
					} else if (!noWarning) {
						JOptionPane.showMessageDialog(null,
						          "Error: Please enter a double bigger than 0", "Error Message",
						          JOptionPane.ERROR_MESSAGE);
					}
		    	} catch(Exception ex) {
		    		if (!noWarning) JOptionPane.showMessageDialog(null,
					          "Error: Please enter a double bigger than 0", "Error Message",
					          JOptionPane.ERROR_MESSAGE);
		    	}
		  	}
		});
		cellSizePanel.add(pixelSizeEntry);
		
		constantsPanel.add(cellSizePanel);
		
		
		JPanel altitudeScalePanel = new JPanel();
		altitudeScalePanel.setLayout(new BoxLayout(altitudeScalePanel, BoxLayout.Y_AXIS));
		JPanel minPanel = new JPanel();
		altitudeScalePanel.add(minPanel);
		minPanel.add(new JLabel("Low (black) in meters:"));
		JPanel maxPanel = new JPanel();
		altitudeScalePanel.add(maxPanel);
		maxPanel.add(new JLabel("High (white) in meters:"));
		
		final JTextField minAltitudeEntry = new JTextField();
		minAltitudeEntry.setText("0.0              ");
		minAltitudeEntry.getDocument().addDocumentListener(new DocumentListener() {
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
		    		double parsed =  Double.parseDouble(minAltitudeEntry.getText());
		    		tempMin = parsed;
		    		if(tempMax > tempMin) {
		    			minAltitude = tempMin;
		    			maxAltitude = tempMax;
		    			// Re-generate water map to reflect change in scale (method will handle case where water map is loaded instead of set)
		    			generateWaterMap();
					} else if (!noWarning) {
						JOptionPane.showMessageDialog(null,
						          "Error: Please enter a double smaller than max", "Error Message",
						          JOptionPane.ERROR_MESSAGE);
					}
		    	} catch(Exception ex) {
		    		if (!noWarning) JOptionPane.showMessageDialog(null,
					          "Error: Please enter a double", "Error Message",
					          JOptionPane.ERROR_MESSAGE);
		    	}
		  	}
		});
		minPanel.add(minAltitudeEntry);
		final JTextField maxAltitudeEntry = new JTextField();
		maxAltitudeEntry.setText("255.0         ");
		maxAltitudeEntry.getDocument().addDocumentListener(new DocumentListener() {
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
		    		double parsed =  Double.parseDouble(maxAltitudeEntry.getText());
		    		tempMax = parsed;
		    		if(tempMax > tempMin) {
		    			minAltitude = tempMin;
		    			maxAltitude = tempMax;
		    			// Re-generate water map to reflect change in scale (method will handle case where water map is loaded instead of set)
		    			generateWaterMap();
					} else if (!noWarning) {
						JOptionPane.showMessageDialog(null,
						          "Error: Please enter a double bigger than min", "Error Message",
						          JOptionPane.ERROR_MESSAGE);
					}
		    	} catch(Exception ex) {
		    		if (!noWarning) JOptionPane.showMessageDialog(null,
					          "Error: Please enter a double", "Error Message",
					          JOptionPane.ERROR_MESSAGE);
		    	}
		  	}
		});
		maxPanel.add(maxAltitudeEntry);
		
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
				String input = JOptionPane.showInputDialog("Altitude of water level (meters)?");
				if (input != null) try {
					waterLevel = Double.valueOf(input);
				} catch (NumberFormatException e) {
					//bad input
					return; //ignore
				} else {
					return; //cancelled
				}
				 if(waterLevel > 0) {
				//generate water map
				generateWaterMap();
				
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
					
					//TODO add UI control for this
					double costDistance = 1.0;
					
					// Perform analysis
					double altitudeScale = (maxAltitude - minAltitude)/255.0;
					analysis = new MapAnalysis(source, start, layers, pixelSize, altitudeScale, costDistance, weightings);
					
					// Update Images
					discreteImage = mapToBufferedImageColor(analysis.discreteCost);
					accumulatedImage = mapToBufferedImageColor(analysis.accumulatedCost);
					pathOverlay = pathOverlayImage(analysis.path);
					updateImages();
					
					// Save analysis data if doSave is checked
					if(doSave.isSelected()) {
						HashMap<String, BufferedImage> analysisData = new HashMap<String, BufferedImage>();
						analysisData.put("discreteMap", discreteImage);
						analysisData.put("accumulatedMap", accumulatedImage);
						analysisData.put("pathMap", pathAndAltitudeToBufferedImage(analysis.path, altitudeLayer)); //generate image on demand
						// Add input files to be saved as well
						analysisData.put("altitudeMap", altitudeImage);
						if(waterLayer != null) analysisData.put("waterMap", waterImage);
						if(roadsLayer != null) analysisData.put("waterMap", roadsImage);
						if(housingLayer != null) analysisData.put("waterMap", housingImage);
						saveAnalysisData(analysisData);
					}
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
				altitudeImage = waterImage = discreteImage = accumulatedImage = null;
				updateImages();
				
				// Reset water level
				waterLevel = 0;
				
				// Un-check doSave
				doSave.setSelected(false);
				
				// Reset text field entries
				noWarning = true;
				// Set all weightings to 0
				for(JTextField weightEntry : weightingEntries) {
					weightEntry.setText("1.0");
				}
				for(MapUtil.MapTypes type : weightings.keySet()) {
					weightings.put(type, 1.0);
				}
				
				// Set pixelSize to default
				pixelSizeEntry.setText("1.0");
				pixelSize = 1.0;
				
				// Set altitude scaling maxAltitudeEntryariables to default
				minAltitudeEntry.setText("0.0");
				tempMin = 0.0;
				maxAltitudeEntry.setText("255.0");
				tempMax = 255.0;
				noWarning = false;
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
		discreteImage = accumulatedImage = null;
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
			altitudePanel.setBackgroundImage(null);
			waterPanel.setBackgroundImage(null);
			roadsPanel.setBackgroundImage(null);
			housingPanel.setBackgroundImage(null);
			discretePanel.setBackgroundImage(null);
			accumulatedPanel.setBackgroundImage(null);
			return;
		}
		int paneWidth = altitudePanel.getWidth();
		int paneHeight = altitudePanel.getHeight();
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
		
		Image scaleOverlay;
		if (pathOverlay != null) {
			scaleOverlay = pathOverlay.getScaledInstance(width, height, Image.SCALE_DEFAULT);
		} else {
			scaleOverlay = null;
		}
		
		updateImage(altitudePanel, altitudeImage, scaleOverlay, width, height);
		updateImage(waterPanel, waterImage, scaleOverlay, width, height);
		updateImage(roadsPanel, roadsImage, scaleOverlay, width, height);
		updateImage(housingPanel, housingImage, scaleOverlay, width, height);
		updateImage(discretePanel, discreteImage, scaleOverlay, width, height);
		updateImage(accumulatedPanel, accumulatedImage, scaleOverlay, width, height);
		
		altitudePanel.setOverlayImage(scaleOverlay);
	}
	//a helper function
	private void updateImage(OverlayPanel panel, BufferedImage image, Image overlay, int width, int height) {
		if (image != null) {
			panel.setBackgroundImage(image.getScaledInstance(width, height, Image.SCALE_DEFAULT));
		} else {
			panel.setBackgroundImage(null);
		}
		panel.setOverlayImage(overlay);
		panel.repaint();
	}


	private void generateWaterMap() {
		if (waterLevel > 0) {
			//generate water map
			int width = altitudeLayer.length;
			int height = altitudeLayer[0].length;
			waterLayer = new double[width][height];
			double altitudeScale = (maxAltitude - minAltitude)/255.0;
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j ++) {
					double altitude = altitudeLayer[i][j]*altitudeScale + minAltitude; // Convert altitudeLayer value to altitude using scale and min value
					if (altitude < waterLevel) {
						waterLayer[i][j] = waterLevel - altitude;
					} else {
						waterLayer[i][j] = 0;
					}
					
				}
			}
			//create image
			waterImage = FileUtil.mapToImage(waterLayer);
			// Update Images
			updateImages();
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
				int scaledValue = (int)(dataMap[i][j]*255/max);
				Color color = new Color(Math.min(255, (int)(255*((scaledValue)/127.0))), Math.min(255, 255 - (int)(255*((scaledValue - 128)/128.0))), 0);
				colorImage.setRGB(i, j, color.getRGB());
			}
		}
		return colorImage;
	}

	/**
	 * Converts path to a BUfferedImage that will show the path as a red line on transparent background
	 * @param path 2D path array that represents a path that will be overlaid on the image
	 * @return BufferedImage
	 */
	private BufferedImage pathOverlayImage(int[][] path) {
		int width = path.length;
		int height = path[0].length;
		BufferedImage pathImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				if (path[i][j] == 1) {
					pathImage.setRGB(i, j, 255<<24 | 255); //opaque, blue
				} else {
					pathImage.setRGB(i, j, 0); //transparent
				}
			}
		}
		return pathImage;
	}

	/**
	 * Converts path and altitudeMap to a BUfferedImage that will show the path as a red line on the altitudeMap
	 * @param path 2D path array that represents a path that will be overlaid on the image
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
