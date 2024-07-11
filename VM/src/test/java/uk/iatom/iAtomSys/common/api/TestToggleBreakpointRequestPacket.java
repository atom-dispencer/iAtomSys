package uk.iatom.iAtomSys.common.api;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.bind.annotation.PostMapping;

public class TestToggleBreakpointRequestPacket {

  @Test
  void err_null() {
    IllegalArgumentException iax = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new ToggleBreakpointRequestPacket(null)
    );
    Assertions.assertEquals(ToggleBreakpointRequestPacket.ERR_NULL, iax.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = { "", " " })
  void err_blank(String blank) {
    IllegalArgumentException iax = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new ToggleBreakpointRequestPacket(blank)
    );
    Assertions.assertEquals(ToggleBreakpointRequestPacket.ERR_BLANK, iax.getMessage());
  }

  @Test
  void success() {
    Assertions.assertDoesNotThrow(
        () -> new ToggleBreakpointRequestPacket("stuff")
    );
  }
}
