package uk.iatom.iAtomSys.common.api;

import java.time.LocalDateTime;

public record RunningDataPacket(LocalDateTime startTime, long executedInstructions) {

}
