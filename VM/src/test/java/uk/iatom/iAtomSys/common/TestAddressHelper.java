package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAddressHelper {

  @Test
  void from_bytes() {
    byte left = 0x01;
    byte right = (byte) 0xa8;

    Assertions.assertEquals((char) 0x01a8, AddressHelper.fromBytes(left, right));
  }
}
