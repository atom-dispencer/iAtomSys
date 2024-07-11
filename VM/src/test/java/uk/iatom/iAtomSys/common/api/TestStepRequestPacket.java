package uk.iatom.iAtomSys.common.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestStepRequestPacket {

  @ParameterizedTest
  @ValueSource(ints = {StepRequestPacket.MIN_COUNT - 1, StepRequestPacket.MAX_COUNT + 1})
  void test_step_countOutside(int count) {
    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new StepRequestPacket(count)
    );

    Assertions.assertEquals(StepRequestPacket.BAD_INTERVAL(count), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {StepRequestPacket.MIN_COUNT + 1, StepRequestPacket.MAX_COUNT - 1})
  void test_step_countInside(int count) {
    Assertions.assertDoesNotThrow(() -> new StepRequestPacket(count));
  }
}
