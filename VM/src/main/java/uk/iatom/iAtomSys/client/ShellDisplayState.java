package uk.iatom.iAtomSys.client;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;

/**
 * Used for drawing the UI. Contains all the state needed to redraw the UI for a single draw call.
 */
@Data
@EqualsAndHashCode
public class ShellDisplayState {

  private String commandMessage;
  private short memorySliceStartAddress;
  private short[] memory;
  private List<String[]> disassembly;
  private List<RegisterPacket> registers;
}
