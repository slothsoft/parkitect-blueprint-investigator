package de.slothsoft.parkitect.blueprint.investigator.step8;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class ImageData {

	public final BufferedImage image;
	public final String json;

	ImageData(BufferedImage image, String json) {
		this.image = Objects.requireNonNull(image);
		this.json = Objects.requireNonNull(json);
	}

}
