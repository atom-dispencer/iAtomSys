package uk.iatom.iAtomSys.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LoadRequestPacket(String imageName) {

  private static final Logger logger = LoggerFactory.getLogger(LoadRequestPacket.class);

  public static final int MAX_PATH_LENGTH = 64;

  public static final String ERR_BLANK = "Image name cannot be blank!";

  public static String ERR_UNSANITARY(String bad, String sanitised) {
    int diff = bad.length() - sanitised.length();
    return "Image name has %d illegal character%s".formatted(diff, Math.abs(diff) == 1 ? "" : "s");
  }

  public static String ERR_LENGTH(int length) {
    return "Image name too long: %d/%d".formatted(length, MAX_PATH_LENGTH);
  }

  public static String sanitise(String name) {
    return name.replaceAll("[^a-zA-Z0-9._]", "");
  }

  public LoadRequestPacket {

    if (imageName == null || imageName.isBlank()) {
      logger.warn(ERR_BLANK);
      throw new IllegalArgumentException(ERR_BLANK);
    }

    int length = imageName.length();
    if (length > MAX_PATH_LENGTH) {
      String message = ERR_LENGTH(length);
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    String sanitised = sanitise(imageName);
    if (!imageName.equals(sanitised)) {
      String message = ERR_UNSANITARY(imageName, sanitised);
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    if (!imageName.endsWith(".img")) {
      imageName = imageName + ".img";
    }
  }
}
