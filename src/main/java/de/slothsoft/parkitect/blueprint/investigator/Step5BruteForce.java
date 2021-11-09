package de.slothsoft.parkitect.blueprint.investigator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import de.slothsoft.parkitect.blueprint.investigator.common.PermutationUtil;

/**
 * This class tries to find the blueprint name in the single bit gotten from the color
 * components.
 */

public class Step5BruteForce {

//	private static final File FILE = new File("blueprints/water-tower-alone.png");
//	private static final String BLUEPRINT_NAME = "water";

	private static final File FILE = new File("blueprints/cone-alone.png");
	private static final String BLUEPRINT_NAME = "cone-alone";

//	private static final File FILE = new File("blueprints/Castle Castle.png");
//	private static final String BLUEPRINT_NAME = "Castke";

	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final int[] PIXEL_POSITIONS = {16, 8, 0, 24};

	public static void main(String[] args) throws IOException {
		final String[] blueprintBytes = convertToByteStrings(BLUEPRINT_NAME);
		System.out.println("Searching for bytes: " + Arrays.stream(blueprintBytes).collect(Collectors.joining(" ")));

		final BufferedImage image = ImageIO.read(FILE);

		for (final int[] positions : PermutationUtil.permute(PIXEL_POSITIONS)) {
			final String[] gameBytes = extractGameBytes(image, positions);

			if (Arrays.equals(positions, PIXEL_POSITIONS)) {
//				printGameBytes(image, gameBytes);
			}

			if (searchForByteStrings(gameBytes, blueprintBytes)) {
				System.out.println("Found at positions: " + Arrays.toString(positions));
			}
		}

		System.out.println("Finished search.");
	}

	/**
	 * This method converts the string to a string representation of the bytes. It uses
	 * the CHARSET constant.
	 */

	private static String[] convertToByteStrings(String string) {
		final byte[] stringBytes = string.getBytes(CHARSET);
		return IntStream.range(0, stringBytes.length).map(i -> stringBytes[i])
				.mapToObj(Step5BruteForce::convertToHexString).toArray(String[]::new);
	}

	/**
	 * Converts integer to hex string.
	 */

	private static String convertToHexString(int number) {
		return String.format("%2s", Integer.toHexString(number)).toUpperCase().replace(' ', '0');
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
					gameBytes[index++] = convertToHexString(Integer.parseInt(lastBits + currentBits, 2));

					if (Arrays.equals(pixelPositions, PIXEL_POSITIONS) && index < 5) {
						System.out.println(lastBits + ' ' + currentBits);
					}

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
	 * Searches the game bytes for the occurrence of the blueprint bytes.
	 */

	private static boolean searchForByteStrings(final String[] gameBytes, final String[] blueprintBytes) {
		for (int g = 0; g < gameBytes.length - blueprintBytes.length; g++) {
			for (int b = 0; b < blueprintBytes.length; b++) {
				if (gameBytes[g + b].equals(blueprintBytes[b])) {
					// blueprint byte is present, but nothing special...
					if (b == blueprintBytes.length - 1) {
						// ...except if it is the last byte
						System.out.println(g);
						System.out.println(Arrays.stream(gameBytes, g, g + blueprintBytes.length)
								.collect(Collectors.joining(" ")));
						return true;
					}
				} else {
					// the blueprint bytes are not at this position
					break;
				}
			}
		}
		return false;
	}

	static void printGameBytes(final BufferedImage image, final String[] gameBytes) {
		System.out.println("\nSearching in data:");
		final int widthOfLine = image.getWidth() / 2;
		for (int y = 0; y < image.getHeight(); y++) {
			final int startIndex = y * widthOfLine;
			System.out.println(Arrays.toString(Arrays.copyOfRange(gameBytes, startIndex, startIndex + widthOfLine)));
		}
	}

}
