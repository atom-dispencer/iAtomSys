package uk.iatom.iAtomSys.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.iatom.iAtomSys.client.configuration.ApiClientConfiguration;
import uk.iatom.iAtomSys.client.disassembly.MemoryDisassembler;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryResponsePacket;
import uk.iatom.iAtomSys.common.api.PortPacket;
import uk.iatom.iAtomSys.common.api.RegisterPacket;
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


  private VmStatus status = VmStatus.STOPPED;
  private String commandMessage = "[Nothing to see here!]";
  private List<String> availableImages = new ArrayList<>();
  private short memorySliceStartAddress = 0;
  private short[] memory = new short[0];
  private List<String[]> disassembly = new ArrayList<>();
  private DebugSymbols debugSymbols = DebugSymbols.empty();
  private RegisterPacket[] registers = new RegisterPacket[0];
  private PortPacket[] ports = new PortPacket[0];

  public void update() {
    status = api.getStatus();

    if (status == null) {
      status = VmStatus.STOPPED;
      commandMessage = "ERROR GETTING STATUS. CHECK LOGS AND REPORT.";
    }

    switch (status) {
      case STOPPED -> updateStopped();
      case PAUSED -> updatePaused();
      case RUNNING -> updateRunning();
    }

  }

  private void updateStopped() {
    if (status != VmStatus.STOPPED) {
      throw new IllegalStateException(
          "Cannot update display state assuming STOPPED status if state is actually " + status);
    }
  }

  /**
   * Update the VM in its {@link VmStatus#PAUSED} state. Fetches the current memory state, register
   * and ports, as well as relevant debug symbols.
   */
  private void updatePaused() {
    if (status != VmStatus.PAUSED) {
      throw new IllegalStateException(
          "Cannot update display state assuming PAUSED status if state is actually " + status);
    }

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
    RegisterPacket[] registers = api.getRegisters();
    if (registers == null) {
      logger.error("Registers are null. Continuing with incomplete data.");
    }
    setRegisters(registers);

    // Update the values of ports
    PortPacket[] ports = api.getPorts();
    if (ports == null) {
      logger.error("Ports are null. Continuing with incomplete data.");
    }
    setPorts(ports);
  }

  private void updateRunning() {
    if (status != VmStatus.RUNNING) {
      throw new IllegalStateException(
          "Cannot update display state assuming RUNNING status if state is actually " + status);
    }
    //TODO Get uptime and FUN data like that!
  }

  /**
   * @return A map of all the memory addresses named by the VM, including
   * {@link uk.iatom.iAtomSys.common.register.Register}s and
   * {@link uk.iatom.iAtomSys.server.device.IOPort}s.
   */
  public Map<Integer, String> getNamedAddresses() {
    Map<Integer, String> addresses = new HashMap<>();

    for (RegisterPacket packet : registers) {
      addresses.putIfAbsent((int) packet.address(), packet.name());
    }

    for (PortPacket packet : ports) {
      addresses.putIfAbsent((int) packet.address(), "IO" + packet.id());
    }

    return addresses;
  }
}
