package de.slothsoft.parkitect.blueprint.investigator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

/**
 * This class searches the last two bits of the RGB values for the blueprint name.
 */

public class Step4TwoBitSearchCheck {

	// bit order: red green blue alpha
	private static final int[] PIXEL_POSITIONS = {17, 16, 9, 8, 1, 0, 25, 24};

	public static void main(String[] args) throws IOException {
		final File file = new File("blueprints/water-tower-alone.png");
		final String searchString = "water-t";

		final BufferedImage image = ImageIO.read(file);
		final String[] gameBytes = new String[image.getWidth() * image.getHeight()];
		int index = 0;

		System.out.println(searchString.chars().mapToObj(Integer::toBinaryString)
				.map(s -> String.format("%8s", s).replace(' ', '0')).collect(Collectors.joining("\t")));

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				final int pixel = image.getRGB(x, y);

				final String currentBits = Arrays.stream(PIXEL_POSITIONS).mapToObj(pos -> getBit(pixel, pos))
						.collect(Collectors.joining());

				gameBytes[index++] = currentBits;
			}
		}

		searchForSimilarByteStrings(gameBytes, searchString);

		System.out.println("Finished search.");
	}

	private static void searchForSimilarByteStrings(final String[] gameBytes, final String searchString) {
		for (int i = 0; i < gameBytes.length - searchString.length(); i++) {
			boolean isSimilar = true;
			for (int j = 0; j < searchString.length(); j++) {
				if (!hasSameOneCount(gameBytes[i + j], Integer.toBinaryString(searchString.charAt(j)))) {
					isSimilar = false;
					break;
				}
			}

			if (isSimilar) {
				System.out.println(i);
				System.out.println(
						Arrays.stream(gameBytes, i, i + searchString.length()).collect(Collectors.joining("\t")));
			}
		}
	}

	private static boolean hasSameOneCount(String string1, String string2) {
		final int oneCount1 = string1.length() - string1.replaceAll("1", "").length();
		final int oneCount2 = string2.length() - string2.replaceAll("1", "").length();
		return oneCount1 == oneCount2;
	}

	private static String getBit(int pixel, int position) {
		return Integer.toString((pixel >> position) & 1);
	}
}
