package de.slothsoft.parkitect.blueprint.investigator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

/**
 * This class extracts the data in the RGB components to console.
 */

public class Step6PrintData {

	private static final File FILE = new File("blueprints/identical/flower-other-creator.png");

	private static final ByteRepresentation BYTE_REPRESENTATION = ByteRepresentation.HEX;
	private static final long BYTE_LIMIT = 24;
	private static final boolean PRETTY_PRINT = true;

	private static final int[] PIXEL_POSITIONS = {24, 0, 8, 16};

	public static void main(String[] args) throws IOException {
		final BufferedImage image = ImageIO.read(FILE);

		final String[] gameBytes = extractGameBytes(image, PIXEL_POSITIONS);

		printGameBytes(gameBytes);
	}

	/**
	 * This method extracts the bits at the pixel position's from the buffered image.
	 */

	private static String[] extractGameBytes(final BufferedImage image, int[] pixelPositions) {
		final String[] gameBytes = new String[image.getWidth() * image.getHeight() / 2];
		int index = 0;
		String lastBits = null;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int pixel = image.getRGB(x, y);

				final String currentBits = Arrays.stream(pixelPositions).mapToObj(pos -> getBit(pixel, pos))
						.collect(Collectors.joining());

				if (lastBits == null) {
					lastBits = currentBits;
				} else {
					gameBytes[index++] = convertToString(Integer.parseInt(currentBits + lastBits, 2));
					lastBits = null;
				}
			}
		}
		return gameBytes;
	}

	private static String getBit(int pixel, int position) {
		return Integer.toString((pixel >> position) & 1);
	}

	/**
	 * Converts integer to a string.
	 */

	private static String convertToString(int number) {
		return BYTE_REPRESENTATION.convertToString(number);
	}

	static void printGameBytes(final String[] gameBytes) {
		String gameBytesString = Arrays.stream(gameBytes).limit(BYTE_LIMIT).collect(Collectors.joining());

		if (PRETTY_PRINT) {
			gameBytesString = gameBytesString
					.replaceAll("(.{" + BYTE_REPRESENTATION.getSeparateAfterBits() + "})", "$1 ")
					.replaceAll("(.{" + BYTE_REPRESENTATION.getLineLength() + "})", "$1\n");
		}
		System.out.println(gameBytesString);
	}

	public enum ByteRepresentation {
		BINARY {

			@Override
			public String convertToString(int number) {
				return String.format("%8s", Integer.toBinaryString(number)).replace(' ', '0');
			}

			@Override
			protected int getSeparateAfterBits() {
				return 8;
			}

			@Override
			protected int getLineLength() {
				return ((getSeparateAfterBits() + 1) * 16);
			}
		},

		HEX {

			@Override
			public String convertToString(int number) {
				return String.format("%2s", Integer.toHexString(number)).toUpperCase().replace(' ', '0');
			}

			@Override
			protected int getSeparateAfterBits() {
				return 2;
			}

			@Override
			protected int getLineLength() {
				return ((getSeparateAfterBits() + 1) * 64);
			}
		},

		DECIMAL {

			@Override
			public String convertToString(int number) {
				return String.format("%3s", Integer.toString(number)).replace(' ', '0');
			}

			@Override
			protected int getSeparateAfterBits() {
				return 3;
			}

			@Override
			protected int getLineLength() {
				return ((getSeparateAfterBits() + 1) * 16);
			}
		},;

		public String convertToString(byte number) {
			return convertToString(number & 0xFF);
		}

		public abstract String convertToString(int number);

		protected abstract int getSeparateAfterBits();

		protected abstract int getLineLength();
	}

}
