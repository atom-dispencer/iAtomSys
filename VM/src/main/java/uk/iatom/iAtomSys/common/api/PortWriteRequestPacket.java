package uk.iatom.iAtomSys.common.api;

import java.util.List;

public record PortWriteRequestPacket(List<Character> data) {

  public static final String ERR_NULL = "Null data";

  public PortWriteRequestPacket {
    if (data == null) {
      throw new IllegalArgumentException(ERR_NULL);
    }
  }
}
