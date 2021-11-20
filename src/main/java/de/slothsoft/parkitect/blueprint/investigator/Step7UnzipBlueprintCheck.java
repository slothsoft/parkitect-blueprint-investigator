package de.slothsoft.parkitect.blueprint.investigator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import de.slothsoft.parkitect.blueprint.investigator.common.PermutationUtil;

/**
 * Reads a file in every pixel position and tries to read a GZIP stream on every byte
 * position.
 */

public class Step7UnzipBlueprintCheck {

	// bit order: red green blue alpha
	private static final int[] PIXEL_POSITIONS = {16, 8, 0, 24};

	public static void main(String[] args) throws IOException {
		final File file = new File("blueprints/identical/flower-1.png");

		for (final int[] pixelPositions : PermutationUtil.permute(PIXEL_POSITIONS)) {
			final byte[] gameBytes = fetchGameBytes(file, pixelPositions);
			unzip(gameBytes, pixelPositions);
		}

		System.out.println("Finished search.");
	}

	private static byte[] fetchGameBytes(final File file, int[] pixelPositions) throws IOException {
		final BufferedImage image = ImageIO.read(file);
		final byte[] gameBytes = new byte[image.getWidth() * image.getHeight() / 2];
		int index = 0;
		String lastBits = null;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int pixel = image.getRGB(x, y);

				final String currentBits = Arrays.stream(pixelPositions).mapToObj(pos -> getBit(pixel, pos))
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

	private static void unzip(final byte[] gameBytes, int[] pixelPositions) {
		for (int i = 3; i < gameBytes.length; i++) {
			try (ByteArrayInputStream ais = new ByteArrayInputStream(gameBytes, i, gameBytes.length - 3);
					GZIPInputStream gis = new GZIPInputStream(ais)) {
				final byte[] buffer = new byte[gameBytes.length];
				while (gis.read(buffer) != -1) {
					final String bufferAsString = new String(buffer);
					System.out.println(Arrays.toString(pixelPositions) + " / " + i + ": " + bufferAsString);
					return;
				}
				System.out.println(i);
			} catch (final IOException e) {
//				System.out.println(e.getMessage());
				continue;
			}
		}
	}
}
