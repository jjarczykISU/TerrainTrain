package fileUtils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FileUtil {
	
	/**
	 * Converts image file to a 2D array
	 * @param file image file
	 * @return 2D array of the interpreted map
	 * @throws IOException if there is a problem reading the file
	 */
	public static int[][] imageToMap(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		Raster raster = image.getData();
		
		int width = raster.getWidth();
		int height = raster.getHeight();
		
		int map[][] = new int[width][height];
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j ++) {
				map[i][j] = raster.getSample(i, j, 0);
			}
		}
		
		return map;
	}
	
	/**
	 * Converts 2D array to grayscale image
	 * @param map array to be converted to image
	 * @return grayscale image interpreted from array
	 */
	public static Image mapToImage(int[][] map) {
		int width = map.length;
		int height = map[0].length;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = image.getRaster();
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j ++) {
				int data = map[i][j];
				raster.setSample(i, j, 0, data);
			}
		}
		
		return image;
	}

}
