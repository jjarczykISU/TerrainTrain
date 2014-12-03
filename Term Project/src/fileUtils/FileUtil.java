package fileUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;


public class FileUtil {
	
	/**
	 * Converts image file to a 2D array
	 * @param file image file
	 * @return 2D array of the interpreted map
	 * @throws IOException if there is a problem reading the file
	 */
	public static double[][] imageToMap(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		double map[][] = new double[width][height];
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j ++) {
				map[i][j] = image.getRGB(i, j) & 255; //mask to only sample first channel
			}
		}
		
		return map;
	}
	
	/**
	 * Converts 2D array to grayscale image
	 * @param map array to be converted to image
	 * @return grayscale image interpreted from array
	 */
	public static BufferedImage mapToImage(double[][] map) {
		int width = map.length;
		int height = map[0].length;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j ++) {
				int data = Double.valueOf(map[i][j]).intValue();
				int rgb = (data<<16) | (data<<8) | (data); //set all color channels to same value for grayscale image
				image.setRGB(i, j, rgb);
			}
		}
		
		return image;
	}

}
