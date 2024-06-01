package uk.iatom.iAtomSys.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.iatom.iAtomSys.client.configuration.ApiClientConfiguration;
import uk.iatom.iAtomSys.client.disassembly.MemoryDisassembler;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryResponsePacket;
import uk.iatom.iAtomSys.common.api.VmClient;
import uk.iatom.iAtomSys.common.api.VmStatus;

/**
 * Used for drawing the UI. Contains all the state needed to redraw the UI for a single draw call.
 */
@Data
@EqualsAndHashCode
public class ShellDisplayState {

  private static final Logger logger = LoggerFactory.getLogger(ShellDisplayState.class);

  @Autowired
  private VmClient api;
  @Autowired
  private MemoryDisassembler memoryDisassembler;
  @Autowired
  private ApiClientConfiguration apiClientConfiguration;


  private VmStatus status;
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

  public void update() {

    VmStatus status = api.getStatus();

    switch (status) {
      case STOPPED -> updateStopped();
      case PAUSED -> updatePaused();
      case RUNNING -> updateRunning();
    }

  }

  private void updateStopped() {
    if (status != VmStatus.STOPPED) {
      throw new IllegalStateException("Cannot update display state assuming STOPPED status if state is actually " + status);
    }
  }

  private void updatePaused() {
    if (status != VmStatus.PAUSED) {
      throw new IllegalStateException("Cannot update display state assuming PAUSED status if state is actually " + status);
    }
    // Show debug info like debug symbols, register values, memory disassembly
    // TODO If the VM is reaches the end of memory, it stops, but does that set a PAUSE or STOP state?

    // TODO (Solved?) Dynamic pcrOffset and sliceWidth
    // Update memory slice and disassembly
    MemoryResponsePacket memoryPacket = api.getMemory(new MemoryRequestPacket(
        apiClientConfiguration.getVmStateRequestPcrOffset(),
        apiClientConfiguration.getVmStateRequestSliceWidth()
    ));

    if (memoryPacket == null) {
      logger.error("Cannot update display VM state: received null.");
      return;
    }

    setMemorySliceStartAddress(memoryPacket.sliceStartAddress());
    short[] memory = memoryPacket.memorySlice();
    List<String[]> disassembly = memoryDisassembler.disassemble(memory);
    setMemory(memory);
    setDisassembly(disassembly);

    // Update register values
    // TODO Register addresses for display should come from debug symbols, not register packets.
    List<RegisterPacket> registers = ;
    setRegisters(registers);

    // Update the values of ports
    // TODO Should ports be only part of debug symbols?
    List<Short> orderedPortAddresses = ;
    setPortAddresses(orderedPortAddresses);
  }

  private void updateRunning() {
    if (status != VmStatus.RUNNING) {
      throw new IllegalStateException("Cannot update display state assuming RUNNING status if state is actually " + status);
    }
    // Get uptime and FUN data like that!
  }
}
