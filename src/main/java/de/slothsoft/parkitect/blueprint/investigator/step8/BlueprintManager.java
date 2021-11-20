package de.slothsoft.parkitect.blueprint.investigator.step8;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

/**
 * Class to read and write blueprints.
 */

public class BlueprintManager {

	private static final int[] PIXEL_POSITIONS = {24, 0, 8, 16};
	private static final int START_BYTE = 23;

	public Blueprint read(InputStream input) throws IOException {
		final BufferedImage image = ImageIO.read(input);

		if (image == null) {
			throw new IOException("Cannot read image.");
		}
		final byte[] gameBytes = fetchGameBytes(image);
		final String json = unzip(gameBytes);
		return new Blueprint(image, json);
	}

	private static byte[] fetchGameBytes(BufferedImage image) {
		final byte[] gameBytes = new byte[image.getWidth() * image.getHeight() / 2];
		int index = 0;
		String lastBits = null;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int pixel = image.getRGB(x, y);

				final String currentBits = Arrays.stream(PIXEL_POSITIONS).mapToObj(pos -> getBit(pixel, pos))
						.collect(Collectors.joining());

				if (lastBits == null) {
					// the first part of the byte
					lastBits = currentBits;
				} else {
					// the last part of the byte
					gameBytes[index++] = (byte) Integer.parseInt(currentBits + lastBits, 2);
					lastBits = null;
				}
			}
		}
		return gameBytes;
	}

	private static String getBit(int pixel, int position) {
		return Integer.toString((pixel >> position) & 1);
	}

	private static String unzip(final byte[] gameBytes) throws IOException {
		final StringBuilder result = new StringBuilder();

		try (InputStream ais = new ByteArrayInputStream(gameBytes, START_BYTE, gameBytes.length - START_BYTE);
				InputStream gis = new GZIPInputStream(ais)) {
			final byte[] buffer = new byte[gameBytes.length];
			while (gis.read(buffer) != -1) {
				result.append(new String(buffer));
			}
			return result.toString();
		}
	}

	public void write(OutputStream output, Blueprint blueprint) throws IOException {
		ImageIO.write(blueprint.image, "PNG", output);
	}
}
