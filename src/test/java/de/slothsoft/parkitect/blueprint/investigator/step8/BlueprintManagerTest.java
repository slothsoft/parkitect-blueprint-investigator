package de.slothsoft.parkitect.blueprint.investigator.step8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BlueprintManagerTest {

	private static final File FILE_FLOWER_1 = new File("blueprints/identical/flower-1.png");

	private TestInfo testInfo;

	private final BlueprintManager manager = new BlueprintManager();

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
	}

	@Test
	void testRead() throws IOException {
		final Blueprint blueprint = this.manager.readFromFile(FILE_FLOWER_1);

		Assertions.assertNotNull(blueprint);
		Assertions.assertNotNull(blueprint.image);
		Assertions.assertEquals(readFileAsString("flower-1-data.txt"), blueprint.json.trim());
	}

	private static String readFileAsString(String fileName) {
		try (Scanner scanner = new Scanner(BlueprintManagerTest.class.getResourceAsStream(fileName))) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next().trim() : "";
		}
	}

	@ParameterizedTest
	@MethodSource("provideFileForWriteSameData")
	void testWriteSameData(File originalFile) throws IOException {
		final Blueprint blueprint = this.manager.readFromFile(originalFile);

		final File copiedFile = writeBlueprintToTemp(blueprint);
		Assertions.assertTrue(copiedFile.exists(), "File should exist: " + copiedFile);

		final Blueprint copiedBlueprint = this.manager.readFromFile(copiedFile);

		Assertions.assertNotNull(copiedBlueprint);
		Assertions.assertNotNull(copiedBlueprint.image);
		Assertions.assertEquals(blueprint.json, copiedBlueprint.json);
	}

	private static Stream<Arguments> provideFileForWriteSameData() {
		return Stream
				.concat(Arrays.stream(new File("blueprints/").listFiles()),
						Arrays.stream(new File("blueprints/identical").listFiles()))
				.filter(f -> f.getName().toLowerCase().endsWith(".png")).map(Arguments::of);
	}

	private File writeBlueprintToTemp(Blueprint blueprint) throws IOException {
		final File result = new File(
				"target/" + this.testInfo.getTestMethod() + "/" + UUID.randomUUID().toString() + ".png");
		result.getParentFile().mkdir();
		try (OutputStream output = new FileOutputStream(result)) {
			this.manager.write(output, blueprint);
		}
		return result;
	}

	@ParameterizedTest
	@MethodSource("provideFileForWriteSameData")
	void testWriteSameDataWipeData(File originalFile) throws IOException {
		final Blueprint blueprint = this.manager.readFromFile(originalFile);

		// wipe the one bit that stores data
		for (int y = 0; y < blueprint.image.getHeight(); y++) {
			for (int x = 0; x < blueprint.image.getWidth(); x++) {
				final int pixel = blueprint.image.getRGB(x, y);
				blueprint.image.setRGB(x, y, pixel & 0xFEFEFEFE);
			}
		}

		final File copiedFile = writeBlueprintToTemp(blueprint);
		Assertions.assertTrue(copiedFile.exists(), "File should exist: " + copiedFile);

		final Blueprint copiedBlueprint = this.manager.readFromFile(copiedFile);

		Assertions.assertNotNull(copiedBlueprint);
		Assertions.assertNotNull(copiedBlueprint.image);
		Assertions.assertEquals(blueprint.json, copiedBlueprint.json);
	}

	@ParameterizedTest
	@MethodSource("provideFileForWriteSameData")
	void testWriteSameDataOtherImage(File originalFile) throws IOException {
		final Blueprint blueprint = this.manager.readFromFile(originalFile);

		final File copiedFile = writeBlueprintToTemp(
				new Blueprint(ImageIO.read(new File("no-blueprint/content-manager-umlauts.png")), blueprint.json));
		Assertions.assertTrue(copiedFile.exists(), "File should exist: " + copiedFile);

		final Blueprint copiedBlueprint = this.manager.readFromFile(copiedFile);

		Assertions.assertNotNull(copiedBlueprint);
		Assertions.assertNotNull(copiedBlueprint.image);
		Assertions.assertEquals(blueprint.json, copiedBlueprint.json);
	}
}
