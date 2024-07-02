package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;

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

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new LoadRequestPacket(imageName),
        LoadRequestPacket.ERR_UNSANITARY(imageName, sanitised)
    );
  }
}
