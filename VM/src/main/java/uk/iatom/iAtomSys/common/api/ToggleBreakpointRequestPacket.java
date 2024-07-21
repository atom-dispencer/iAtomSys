package uk.iatom.iAtomSys.common.api;

public record ToggleBreakpointRequestPacket(String addressStr) {

  public static final String NEXT = "next";
  public static final String HERE = "here";
  public static final String PREV = "prev";
  public static final String NUKE = "nuke";

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
