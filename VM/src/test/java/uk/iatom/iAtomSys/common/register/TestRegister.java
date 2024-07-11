package uk.iatom.iAtomSys.common.register;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.iatom.iAtomSys.server.memory.Memory;

public class TestRegister {

  @Mock
  Memory memory;

  Register register;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    register = new Register("TST", 0, (char) 0, memory);
  }

  @Test
  void set() {
    char newValue = (char) 10;
    register.set(newValue);
    verify(memory, times(1)).write(register.getAddress(), newValue);
  }
}
