package uk.iatom.iAtomSys.client.shell;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Used for drawing the UI. Contains all the state needed to redraw the UI for a single draw call.
 */
@Data
@EqualsAndHashCode
public class ShellDisplayState {

  private String commandMessage;
  private byte[] memoryState;
}
