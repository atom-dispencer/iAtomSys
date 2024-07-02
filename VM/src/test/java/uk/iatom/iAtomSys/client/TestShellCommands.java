package uk.iatom.iAtomSys.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.iatom.iAtomSys.common.api.VmClient;

public class TestShellCommands {

  @Mock
  ShellDisplay display;

  @Mock
  VmClient api;

  ShellCommands shellCommands;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    Mockito.doNothing().when(display).activate();
    Mockito.when(display.getDisplayState()).thenReturn(new ShellDisplayState());
    shellCommands = new ShellCommands(api, display);
  }

  @ParameterizedTest
  @ValueSource(strings = { "", "abc", "$%^", "100a", "\na" })
  void test_help_numberFormat(String str) {
    shellCommands.help(str);
    Assertions.assertEquals(
        ShellCommands.HELP_BAD_FORMAT.apply(str),
        shellCommands.getDisplay().getDisplayState().getCommandMessage()
    );
  }

  @ParameterizedTest
  @ValueSource(ints = { -1, Integer.MAX_VALUE, Integer.MIN_VALUE })
  void test_help_index(int index) {
    String str = Integer.toString(index);
    shellCommands.help(str);

    Assertions.assertEquals(
        ShellCommands.HELP_BAD_INDEX.apply(str),
        shellCommands.getDisplay().getDisplayState().getCommandMessage()
    );
  }
}
