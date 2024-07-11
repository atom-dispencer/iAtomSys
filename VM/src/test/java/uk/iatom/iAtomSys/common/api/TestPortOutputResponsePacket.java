package uk.iatom.iAtomSys.common.api;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPortOutputResponsePacket {

  @Test
  void err_null() {
    IllegalArgumentException iax = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new PortOutputResponsePacket(null)
    );
    Assertions.assertEquals(PortOutputResponsePacket.ERR_NULL, iax.getMessage());
  }

  @Test
  void success() {
    Assertions.assertDoesNotThrow(
        () -> new PortOutputResponsePacket(new ArrayList<>())
    );
  }
}
