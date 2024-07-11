package uk.iatom.iAtomSys.common.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestLoadRequestPacket {


  @Test
  void name_null() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new LoadRequestPacket(null)
    );
  }

  @Test
  void name_blank() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new LoadRequestPacket("")
    );
  }

  @ParameterizedTest
  @ValueSource(strings = { "bob?", "bo/b" })
  void sanitise(String name) {
    Assertions.assertEquals(
        "bob",
        LoadRequestPacket.sanitise(name)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = { "bob?", "bo/b", "../../bob" })
  void unsanitary(String imageName) {
    String sanitised = LoadRequestPacket.sanitise(imageName);

    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new LoadRequestPacket(imageName)
    );

    Assertions.assertEquals(
        LoadRequestPacket.ERR_UNSANITARY(imageName, sanitised),
        ex.getMessage()
    );
  }

  @Test
  void name_long() {
    String name = "x".repeat(LoadRequestPacket.MAX_PATH_LENGTH + 1);

    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new LoadRequestPacket(name)
    );

    Assertions.assertEquals(LoadRequestPacket.ERR_LENGTH(name.length()), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = { "test", "test.img" })
  void success(String name) {
    LoadRequestPacket packet = new LoadRequestPacket(name);
    Assertions.assertEquals("test.img", packet.imageName());
  }
}
