package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  @ParameterizedTest
  @ValueSource(strings = { "a", "ab", "abcde" })
  void address_length_bad(String address) {
    IllegalStateException isx = Assertions.assertThrows(
        IllegalStateException.class,
        () -> new SetRequestPacket(address, "0000")
    );
    Assertions.assertEquals(SetRequestPacket.ERR_BAD_ADDRESS_LENGTH.apply(address), isx.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = { "a", "ab", "abcde" })
  void value_length_bad(String address) {
    IllegalStateException isx = Assertions.assertThrows(
        IllegalStateException.class,
        () -> new SetRequestPacket("0000", address)
    );
    Assertions.assertEquals(SetRequestPacket.ERR_BAD_VALUE_LENGTH.apply(address), isx.getMessage());
  }

  @Test
  void success_address_longer() {
    String address = "addr";
    String value = "val";

    Assertions.assertDoesNotThrow(
        () -> new SetRequestPacket(address, value)
    );
  }


  @Test
  void success_value_longer() {
    String address = "add";
    String value = "valu";

    Assertions.assertDoesNotThrow(
        () -> new SetRequestPacket(address, value)
    );
  }
}
