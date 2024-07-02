package uk.iatom.iAtomSys.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;

public class TestStepRequestPacket {

  @ParameterizedTest
  @ValueSource(ints = { StepRequestPacket.MIN_COUNT - 1, StepRequestPacket.MAX_COUNT + 1 })
  void test_step_countOutside(int count) {
    String expected = StepRequestPacket.BAD_INTERVAL(count);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new StepRequestPacket(count),
        expected
    );
  }

  @ParameterizedTest
  @ValueSource(ints = { StepRequestPacket.MIN_COUNT + 1, StepRequestPacket.MAX_COUNT - 1 })
  void test_step_countInside(int count) {
    Assertions.assertDoesNotThrow(() -> new StepRequestPacket(count));
  }
}
