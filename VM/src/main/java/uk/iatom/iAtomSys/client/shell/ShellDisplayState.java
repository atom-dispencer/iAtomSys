package uk.iatom.iAtomSys.client.shell;

import java.time.LocalDateTime;
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
import uk.iatom.iAtomSys.common.api.RunningDataPacket;
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
  private String[] availableImages = new String[0];
  private char memorySliceStartAddress = 0;
  private char[] memory = new char[0];
  private List<String[]> disassembly = new ArrayList<>();
  private DebugSymbols debugSymbols = DebugSymbols.empty();
  private RegisterPacket[] registers = new RegisterPacket[0];
  private PortPacket[] ports = new PortPacket[0];
  private LocalDateTime runningSince = LocalDateTime.now();
  private Character[] breakpoints = new Character[0];
  private long runningInstructionsExecuted = 0L;

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

    // Available memory images to display on the STOPPED message
    String[] images = api.getAvailableImages();
    if (images == null) {
      logger.error("Available images are null. Continuing with incomplete data.");
    }
    setAvailableImages(images);
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
    char[] memory = memoryPacket.memorySlice();
    List<String[]> disassembly = memoryDisassembler.disassemble(memory);
    setMemory(memory);
    setDisassembly(disassembly);

    Character[] breakpoints = api.getBreakpoints();
    setBreakpoints(breakpoints);

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

    RunningDataPacket packet = api.getRunningData();
    if (packet == null) {
      logger.error("Asynchronous running data is null. Continuing with incomplete data.");
    } else {
      LocalDateTime start = packet.startTime();
      setRunningSince(start == null ? LocalDateTime.now() : start);

      setRunningInstructionsExecuted(packet.executedInstructions());
    }
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
