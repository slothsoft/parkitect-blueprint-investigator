package de.slothsoft.parkitect.blueprint.investigator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * Test to see if we can read a savegame file with the default Java
 * {@link GZIPInputStream}.
 */

public class Step7UnzipSavegameCheck {

	public static void main(String[] args) {
		final File file = new File("no-blueprint/savegame.park");

		try (InputStream is = new FileInputStream(file); InputStream gis = new GZIPInputStream(is)) {
			final byte[] buffer = new byte[128];
			while (gis.read(buffer) != -1) {
				System.out.println(new String(buffer));
				System.out.println(Arrays.toString(Arrays.copyOf(buffer, 2)));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		System.out.println("Finished unzip.");
	}
}
