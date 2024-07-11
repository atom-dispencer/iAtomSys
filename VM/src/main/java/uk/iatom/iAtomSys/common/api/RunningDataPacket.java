package uk.iatom.iAtomSys.common.api;

import java.time.LocalDateTime;

public record RunningDataPacket(LocalDateTime startTime, long executedInstructions) {

  public static final String ERR_NULL = "Null start time.";

  public RunningDataPacket {
    if (startTime == null) {
      throw new IllegalArgumentException(ERR_NULL);
    }
  }
}
