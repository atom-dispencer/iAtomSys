package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;

public class TestMemoryRequestPacket {

  @ParameterizedTest
  @ValueSource( shorts = { Short.MIN_VALUE, -1, MemoryRequestPacket.MAX_SLICE_WIDTH + 1, Short.MAX_VALUE})
  void width_exceeds_interval(short width) {
    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new MemoryRequestPacket((short) 0, width)
    );

    Assertions.assertEquals(MemoryRequestPacket.ERR_WIDTH(width), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(shorts = { 0, MemoryRequestPacket.MAX_SLICE_WIDTH })
  void success(short width) {
    Assertions.assertDoesNotThrow(
        () -> new MemoryRequestPacket((short) 0, width)
    );
  }

}
