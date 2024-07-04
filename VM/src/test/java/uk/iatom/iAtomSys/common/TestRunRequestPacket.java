package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.iatom.iAtomSys.common.api.RunRequestPacket;

public class TestRunRequestPacket {

  @Test
  void address_null() {
    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new RunRequestPacket(null)
    );

    Assertions.assertEquals(RunRequestPacket.ERR_ADDRESS_NULL, ex.getMessage());
  }

  @Test
  void address_long() {
    String address = "x".repeat(RunRequestPacket.MAX_LENGTH + 1);

    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new RunRequestPacket(address)
    );

    Assertions.assertEquals(RunRequestPacket.ERR_ADDRESS_LONG(address), ex.getMessage());
  }

  @Test
  void address_blank() {
    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new RunRequestPacket("")
    );

    Assertions.assertEquals(RunRequestPacket.ERR_ADDRESS_BLANK, ex.getMessage());
  }

  @Test
  void success() {
    Assertions.assertDoesNotThrow(
        () -> new RunRequestPacket("address")
    );
  }
}
