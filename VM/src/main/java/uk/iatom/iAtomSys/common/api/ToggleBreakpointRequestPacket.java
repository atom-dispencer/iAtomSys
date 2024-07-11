package uk.iatom.iAtomSys.common.api;

public record ToggleBreakpointRequestPacket(String addressStr) {

  public static final String ERR_NULL = "AddressStr cannot be null";
  public static final String ERR_BLANK = "AddressStr cannot be blank";

  public ToggleBreakpointRequestPacket {
    if (addressStr == null) {
      throw new IllegalArgumentException(ERR_NULL);
    }

    if (addressStr.isBlank()) {
      throw new IllegalArgumentException(ERR_BLANK);
    }
  }
}
