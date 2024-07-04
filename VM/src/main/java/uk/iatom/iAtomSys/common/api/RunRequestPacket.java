package uk.iatom.iAtomSys.common.api;

public record RunRequestPacket(String startAddress) {

  public static final int MAX_LENGTH = 10;
  public static final String ERR_ADDRESS_NULL = "Start address may not be null";
  public static final String ERR_ADDRESS_BLANK = "Start address may not be blank";

  public static String ERR_ADDRESS_LONG(String startAddress) {
    return"Start address is too long: %d>%d".formatted(startAddress.length(), MAX_LENGTH);
  }

  public RunRequestPacket {

    if (startAddress == null) {
      throw new IllegalArgumentException(ERR_ADDRESS_NULL);
    }

    if (startAddress.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(ERR_ADDRESS_LONG(startAddress));
    }

    if (startAddress.isBlank()) {
      throw new IllegalArgumentException(ERR_ADDRESS_BLANK);
    }
  }
}
