package de.slothsoft.parkitect.blueprint.investigator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class prints the data stored in the last the bit of RGB values.
 */

public class Step3RgbDataCheck {

	public static void main(String[] args) throws IOException {
		final File file = new File("blueprints/water-tower-alone.png");

		final BufferedImage image = ImageIO.read(file);

		final byte[] gameBytes = new byte[image.getWidth() * image.getHeight() / 2];
		int index = 0;
		String lastBits = null;

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				final int pixel = image.getRGB(x, y);

				// bit order: red green blue alpha
				final String currentBits = getBit(pixel, 16) + getBit(pixel, 8) + getBit(pixel, 0) + getBit(pixel, 24);

				if (lastBits == null) {
					// the first part of the byte
					lastBits = currentBits;
				} else {
					// the last part of the byte
					gameBytes[index++] = (byte) Integer.parseInt(lastBits + currentBits, 2);
					lastBits = null;
				}
			}
		}
		System.out.println(new String(gameBytes).replaceAll("(.{100})", "$1\n"));
	}

	private static String getBit(int pixel, int position) {
		return Integer.toString((pixel >> position) & 1);
	}
}
