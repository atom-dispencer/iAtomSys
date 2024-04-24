package uk.iatom.iAtomSys.common.api;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LoadImageRequestPacket(String imageName) {

  private static final Logger logger = LoggerFactory.getLogger(LoadImageRequestPacket.class);
  private static final int MAX_PATH_LENGTH = 64;

  public LoadImageRequestPacket {

    if (imageName == null || imageName.isBlank()) {
      String message = "Image name cannot be blank!";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    if (!imageName.endsWith(".img")) {
      imageName = imageName + ".img";
    }

    String fileNameOnly = new File(imageName).getName();
    if (!fileNameOnly.equals(imageName)) {
      String message = "Image name must be simple: %s != %s".formatted(fileNameOnly, imageName);
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    int length = imageName.length();
    if (length > MAX_PATH_LENGTH) {
      String message = "Image name too long: %d/%d".formatted(length, MAX_PATH_LENGTH);
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }
  }
}
