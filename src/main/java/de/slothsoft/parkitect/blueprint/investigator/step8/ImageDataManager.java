package de.slothsoft.parkitect.blueprint.investigator.step8;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

/**
 * Class to read and write blueprints to the data stored in the image.
 */

public class ImageDataManager {

	private static final int[] PIXEL_POSITIONS = {24, 0, 8, 16};
	private static final int[] MAGIC_NUMBER = {0x53, 0x4D, 0x01};

	private static final int BYTE_MAGIC_NUMBER = 0;
	private static final int BYTE_LENGTH = BYTE_MAGIC_NUMBER + MAGIC_NUMBER.length;
	private static final int BYTE_CHECKSUM = BYTE_LENGTH + 4;
	static final int BYTE_GZIP_START = BYTE_CHECKSUM + 16;

	private Charset charset = Charset.forName("utf8");
	private final MessageDigest messageDigest;

	public ImageDataManager() {
		try {
			this.messageDigest = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("Something went very wrong if MD5 could not be found.");
		}
	}

	public ImageData readFromFile(File file) throws IOException {
		try (InputStream input = new FileInputStream(file)) {
			return read(input);
		}
	}

	public ImageData read(InputStream input) throws IOException {
		final BufferedImage image = ImageIO.read(input);

		if (image == null) {
			throw new IOException("Cannot read image.");
		}
		final byte[] gameBytes = fetchGameBytes(image);
		final String json = unzip(gameBytes);
		return new ImageData(image, json);
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

	private String unzip(final byte[] gameBytes) throws IOException {
		final long length = (((long) 0xFF & gameBytes[6]) << 24) | ((0xFF & gameBytes[5]) << 16)
				| ((0xFF & gameBytes[4]) << 8) | (0xFF & gameBytes[3]);

		try (InputStream ais = new ByteArrayInputStream(gameBytes, BYTE_GZIP_START, (int) length);
				InputStream gis = new GZIPInputStream(ais);
				Scanner scanner = new Scanner(gis)) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	public void write(OutputStream output, ImageData blueprint) throws IOException {
		final byte[] gzipData = zip(blueprint.json);
		if (gzipData == null || gzipData.length == 0) {
			throw new IOException("Could not create GZIP correctly!");
		}
		final byte[] gameBytes = enrichGzipData(gzipData);
		setGameBytes(blueprint.image, gameBytes);
		ImageIO.write(blueprint.image, "PNG", output);
	}

	private byte[] zip(String json) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (GZIPOutputStream gos = new GZIPOutputStream(bos)) {
			gos.write(json.getBytes(this.charset));
		}
		return bos.toByteArray();
	}

	private byte[] enrichGzipData(byte[] gzipData) throws IOException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// magic number
		for (int i = 0; i < MAGIC_NUMBER.length; i++) {
			outputStream.write(MAGIC_NUMBER[i]);
		}

		// gzip length
		final byte[] bytes = ByteBuffer.allocate(4).putInt(gzipData.length).array();
		for (int i = bytes.length - 1; i >= 0; i--) {
			// the bytes are reversed; ByteBuffer: 0, 0, 2, 29 & stream 29, 2, 0, 0
			outputStream.write(bytes[i]);
		}

		// checksum
		outputStream.write(createChecksum(gzipData));

		// actual data
		outputStream.write(gzipData);
		outputStream.flush();

		return outputStream.toByteArray();
	}

	private byte[] createChecksum(byte[] gzipData) {
		this.messageDigest.update(gzipData);
		return this.messageDigest.digest();
	}

	private static void setGameBytes(BufferedImage image, byte[] gameBytes) {
		int index = 0;
		boolean lastBits = false;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {

				// prepare the byte
				String byteString;
				if (index < gameBytes.length) {
					byteString = convertToBinaryString(gameBytes[index]);
					byteString = lastBits ? byteString.substring(0, 4) : byteString.substring(4);
				} else {
					byteString = "0000";
				}

				// flip the bits of the color components
				int pixel = image.getRGB(x, y);
				for (int i = 0; i < PIXEL_POSITIONS.length; i++) {
					pixel = setBit(pixel, PIXEL_POSITIONS[i], byteString.charAt(i) == '1');
				}
				image.setRGB(x, y, pixel);

				// increment
				if (lastBits) {
					index++;
					lastBits = false;
				} else {
					lastBits = true;
				}
			}
		}
	}

	private static String convertToBinaryString(byte b) {
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}

	private static int setBit(int pixel, int pos, boolean value) {
		if (value) {
			return pixel | (1 << pos);
		}
		return pixel & ~(1 << pos);
	}

	public Charset getCharset() {
		return this.charset;
	}

	public ImageDataManager charset(Charset newCharset) {
		setCharset(newCharset);
		return this;
	}

	public void setCharset(Charset charset) {
		this.charset = Objects.requireNonNull(charset);
	}

}
