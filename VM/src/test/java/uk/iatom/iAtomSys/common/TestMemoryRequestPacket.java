package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;

public class TestMemoryRequestPacket {

  @ParameterizedTest
  @ValueSource( chars = { MemoryRequestPacket.MAX_SLICE_WIDTH + 1, Character.MAX_VALUE})
  void width_exceeds_interval(char width) {
    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new MemoryRequestPacket((char) 0, width)
    );

    Assertions.assertEquals(MemoryRequestPacket.ERR_WIDTH(width), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(chars = { 0, MemoryRequestPacket.MAX_SLICE_WIDTH })
  void success(char width) {
    Assertions.assertDoesNotThrow(
        () -> new MemoryRequestPacket((char) 0, width)
    );
  }

}
