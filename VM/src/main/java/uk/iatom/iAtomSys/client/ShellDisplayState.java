package uk.iatom.iAtomSys.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;

/**
 * Used for drawing the UI. Contains all the state needed to redraw the UI for a single draw call.
 */
@Data
@EqualsAndHashCode
public class ShellDisplayState {

  private boolean running;
  private String commandMessage;
  private List<String> availableImages;
  private short memorySliceStartAddress;
  private short[] memory;
  private List<String[]> disassembly;
  private List<RegisterPacket> registers;
  private List<Short> portAddresses;

  public Map<Integer, String> getReservedAddresses() {
    Map<Integer, String> reservedAddresses = new HashMap<>();

    if (getRegisters() != null) {
      for (RegisterPacket registerPacket : getRegisters()) {
        reservedAddresses.put((int) registerPacket.address(), registerPacket.name());
      }
    }

    if (getPortAddresses() != null) {
      for (int portNum = 0; portNum < getPortAddresses().size(); portNum++) {
        Short portAddress = getPortAddresses().get(portNum);
        reservedAddresses.put(portAddress.intValue(), "IO" + portNum);
      }
    }

    return reservedAddresses;
  }
}
