package uk.iatom.iAtomSys.common.api;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestRunningDataPacket {

  @Test
  void err_null() {
    IllegalArgumentException iax = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new RunningDataPacket(null, 0L)
    );
    Assertions.assertEquals(RunningDataPacket.ERR_NULL, iax.getMessage());
  }

  @Test
  void success() {
    Assertions.assertDoesNotThrow(
        () -> new RunningDataPacket(LocalDateTime.now(), 0L)
    );
  }
}
