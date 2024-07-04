package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;

public class TestSetRequestPacket {

  @Test
  void address_null() {
    IllegalStateException ex = Assertions.assertThrows(
        IllegalStateException.class,
        () -> new SetRequestPacket(null, null)
    );

    Assertions.assertEquals(SetRequestPacket.ERR_ADDRESS_NULL, ex.getMessage());
  }

  @Test
  void value_null() {
    String address = "";

    IllegalStateException ex = Assertions.assertThrows(
        IllegalStateException.class,
        () -> new SetRequestPacket(address, null)
    );

    Assertions.assertEquals(SetRequestPacket.ERR_VALUE_NULL, ex.getMessage());
  }

  @Test
  void address_blank() {
    String address = "";
    String value = "value";

    IllegalStateException ex = Assertions.assertThrows(
        IllegalStateException.class,
        () -> new SetRequestPacket(address, value)
    );

    Assertions.assertEquals(SetRequestPacket.ERR_ADDRESS_BLANK, ex.getMessage());
  }

  @Test
  void value_blank() {
    String address = "address";
    String value = "";

    IllegalStateException ex = Assertions.assertThrows(
        IllegalStateException.class,
        () -> new SetRequestPacket(address, value)
    );

    Assertions.assertEquals(SetRequestPacket.ERR_VALUE_BLANK, ex.getMessage());
  }

  @Test
  void success() {
    String address = "address";
    String value = "value";

    Assertions.assertDoesNotThrow(
        () -> new SetRequestPacket(address, value)
    );
  }
}
