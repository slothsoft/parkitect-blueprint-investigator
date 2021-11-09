package de.slothsoft.parkitect.blueprint.investigator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class reads PNG files according to spec.
 *
 * @see "http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html"
 */

public class Step2ImageChunksCheck {

	private static final byte[] PNG_START = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};

	public static void main(String[] args) throws IOException {
		final File file = new File("blueprints/water-tower-alone.png");
		final long fileLength = file.length();
		final byte[] fileContent = new byte[(int) fileLength];

		try (final FileInputStream inputStream = new FileInputStream(file)) {
			if (inputStream.read(fileContent) == -1) throw new IllegalArgumentException("Some things suck right now.");
		}

//		System.out.println(new String(fileContent));

		parseFileContent(fileContent);
	}

	private static void parseFileContent(byte[] fileContent) {

		// check starting bytes

		for (int i = 0; i < PNG_START.length; i++) {
			if (fileContent[i] != PNG_START[i]) throw new IllegalArgumentException("This file is no PNG!");
		}
		System.out.println("File is really PNG.");

		int index = PNG_START.length;
		int count = 1;

		while (index < fileContent.length) {
			System.out.println("\n\nChunk " + count++ + "\n=======");
			final int length = parseNextChunk(fileContent, index);
			index += length;
		}
	}

	/**
	 * @see "http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html"
	 */

	private static int parseNextChunk(byte[] fileContent, int index) {
		// Length
		// A 4-byte unsigned integer giving the number of bytes in the chunk's data
		// field. The length counts only the data field, not itself, the chunk type code,
		// or the CRC. Zero is a valid length. Although encoders and decoders should treat
		// the length as unsigned, its value must not exceed 231 bytes.
		final int lengthBytes = 4;
		final int length = ByteBuffer.wrap(fileContent, index, lengthBytes).getInt();
		System.out.println("Length: " + length);

		// Chunk Type
		// A 4-byte chunk type code. For convenience in description and in examining PNG
		// files, type codes are restricted to consist of uppercase and lowercase ASCII
		// letters (A-Z and a-z, or 65-90 and 97-122 decimal). However, encoders and
		// decoders must treat the codes as fixed binary values, not character strings.
		// For example, it would not be correct to represent the type code IDAT by the
		// EBCDIC equivalents of those letters. Additional naming conventions for chunk
		// types are discussed in the next section.
		final int typeBytes = 4;
		final String type = new String(fileContent, index + lengthBytes, typeBytes);
		System.out.println("Type: " + type);

		System.out.println("\tAncillary bit: " + getBit(fileContent[index + lengthBytes], 5));
//		System.out.println("\tPrivate bit: " + getBit(fileContent[index + lengthBytes + 1], 5));
//		System.out.println("\tZero bit: " + getBit(fileContent[index + lengthBytes + 2], 5));
//		System.out.println("\tSafe-to-copy bit: " + getBit(fileContent[index + lengthBytes + 3], 5));

		// Chunk Data
		// The data bytes appropriate to the chunk type, if any. This field can be of
		// zero length.
//		System.out.println("Chunk Data: " + (length > 0));

		// CRC
		// A 4-byte CRC (Cyclic Redundancy Check) calculated on the preceding bytes in
		// the chunk, including the chunk type code and chunk data fields, but not
		// including the length field. The CRC is always present, even for chunks
		// containing no data.
		final int crcBytes = 4;

		return length + lengthBytes + typeBytes + crcBytes;
	}

	private static boolean getBit(byte b, int position) {
		return ((b >> position) & 1) == 1;
	}
}
