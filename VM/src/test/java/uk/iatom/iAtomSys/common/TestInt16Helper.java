package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestInt16Helper {

  @Test
  void from_bytes() {
    byte left = 0x01;
    byte right = (byte) 0xa8;

    Assertions.assertEquals((char) 0x01a8, Int16Helper.fromBytes(left, right));
  }
}
