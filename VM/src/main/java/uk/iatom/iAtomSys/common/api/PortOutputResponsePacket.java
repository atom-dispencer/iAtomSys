package uk.iatom.iAtomSys.common.api;

import java.util.List;

public record PortOutputResponsePacket(List<Character> outputBuffer) {

  public static final String ERR_NULL = "Null outputBuffer";

  public PortOutputResponsePacket {
    if (outputBuffer == null) {
      throw new IllegalArgumentException(ERR_NULL);
    }
  }
}
