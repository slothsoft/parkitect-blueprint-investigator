package de.slothsoft.parkitect.blueprint.investigator.step8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BlueprintManagerTest {

	private final BlueprintManager manager = new BlueprintManager();

	@Test
	public void testRead() throws IOException {
		final Blueprint blueprint = readBlueprint(new File("blueprints/identical/flower-1.png"));

		Assertions.assertNotNull(blueprint);
		Assertions.assertNotNull(blueprint.image);
		Assertions.assertEquals(readFileAsString("flower-1-data.txt"), blueprint.json.trim());
	}

	private Blueprint readBlueprint(File file) throws IOException {
		try (InputStream input = new FileInputStream(file)) {
			return this.manager.read(input);
		}
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
		final Blueprint blueprint = readBlueprint(originalFile);

		final File copiedFile = writeBlueprintToTemp(blueprint);
		Assertions.assertTrue(copiedFile.exists(), "File should exist: " + copiedFile);

		final Blueprint copiedBlueprint = readBlueprint(copiedFile);

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
		final File result = new File("target/" + UUID.randomUUID().toString() + ".png");
		try (OutputStream output = new FileOutputStream(result)) {
			this.manager.write(output, blueprint);
		}
		return result;
	}
}
