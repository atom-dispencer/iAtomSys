package uk.iatom.iAtomSys.common.api;

public record RunRequestPacket(String startAddress) {

  public RunRequestPacket {

    if (startAddress == null) {
      throw new IllegalStateException("Start address may not be null");
    }

    int MAX_LENGTH = 10;
    if (startAddress.length() > MAX_LENGTH) {
      throw new IllegalStateException(
          "Start address is too long: %d>%d".formatted(startAddress.length(), MAX_LENGTH));
    }

    if (startAddress.isBlank()) {
      throw new IllegalStateException("Start address may not be blank");
    }
  }
}
