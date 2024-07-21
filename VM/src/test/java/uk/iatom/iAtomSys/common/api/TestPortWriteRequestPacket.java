package uk.iatom.iAtomSys.common.api;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPortWriteRequestPacket {

  @Test
  void err_null() {
    IllegalArgumentException iax = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new PortWriteRequestPacket(null)
    );
    Assertions.assertEquals(PortWriteRequestPacket.ERR_NULL, iax.getMessage());
  }

  @Test
  void success() {
    Assertions.assertDoesNotThrow(
        () -> new PortWriteRequestPacket(new ArrayList<>())
    );
  }
}
