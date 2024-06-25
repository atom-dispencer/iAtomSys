package uk.iatom.iAtomSys.server.net;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.DebugSymbolsRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryResponsePacket;
import uk.iatom.iAtomSys.common.api.PortOutputResponsePacket;
import uk.iatom.iAtomSys.common.api.PortPacket;
import uk.iatom.iAtomSys.common.api.PortWriteRequestPacket;
import uk.iatom.iAtomSys.common.api.RegisterPacket;
import uk.iatom.iAtomSys.common.api.RunningDataPacket;
import uk.iatom.iAtomSys.common.api.VmStatus;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.AsyncRunData;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.server.device.IOPort;

@RestController
@RequestMapping("/state")
public class StateRestController {

  private final Logger logger = LoggerFactory.getLogger(StateRestController.class);

  @Autowired
  private IAtomSysVM vm;

  @GetMapping("/status")
  public VmStatus status() {
    return vm.getStatus();
  }

  @GetMapping("/running_data")
  public RunningDataPacket runningData() {
    AsyncRunData runData = vm.getAsyncRunData();

    return new RunningDataPacket(runData.getStartTime(), runData.getAsyncExecutedInstructions().get());
  }

  @GetMapping("/images")
  public String[] images() {

    File imagesDirectory = new File("images/");
    String[] imageFileNames = imagesDirectory.list((file, name) -> name.endsWith(".img"));

    if (imageFileNames == null) {
      return new String[0];
    }

    String[] result = new String[imageFileNames.length];
    for (int i = 0; i < imageFileNames.length; i++) {

      // Collect the names, removing the file extension
      String name = imageFileNames[i];
      result[i] = name.substring(0, name.length() - 4);
    }
    return result;
  }

  @GetMapping("/registers")
  public RegisterPacket[] registers() {

    RegisterSet regs = vm.getRegisterSet();
    return new RegisterPacket[]{
        Register.PCR(regs).toPacket(),
        Register.FLG(regs).toPacket(),
        Register.ACC(regs).toPacket(),
        Register.IDK(regs).toPacket(),
        Register.TBH(regs).toPacket(),
        Register.LOL(regs).toPacket()
    };
  }

  @PostMapping("/memory")
  public MemoryResponsePacket memory(@RequestBody MemoryRequestPacket packet) {

    // Prepare memorySlice region
    short pcr = Register.PCR(vm.getRegisterSet()).get();
    int width = Math.max(0, packet.sliceWidth());

    int startAddress = Math.max(0, pcr + packet.pcrOffset());
    int endAddress = Math.min(Short.MAX_VALUE, startAddress + width);
    short clampedStartAddress = (short) Math.min(Short.MAX_VALUE, startAddress);
    short clampedEndAddress = (short) Math.max(endAddress, clampedStartAddress);

    short clampedWidth = (short) (clampedEndAddress - clampedStartAddress);

    short[] memory = new short[clampedWidth];
    vm.getMemory().readRange(clampedStartAddress, memory);

    return new MemoryResponsePacket(clampedStartAddress, memory);
  }

  @GetMapping("/debug_symbols")
  public DebugSymbols debugSymbols(@RequestBody DebugSymbolsRequestPacket packet) {
    DebugSymbols currentDebugSymbols = vm.getDebugSymbols();
    return currentDebugSymbols.takeRelevant(packet.startAddress(), packet.endAddress());
  }

  @GetMapping("/ports")
  public PortPacket[] ports() {
    IOPort[] ports = vm.getPorts();
    PortPacket[] destination = new PortPacket[ports.length];

    for (int i = 0; i < ports.length; i++) {
      IOPort p = ports[i];
      destination[i] = new PortPacket(i, p.getAddress(), p.peek(), p.getFlag());
    }

    return destination;
  }

  /**
   * Read any as-yet unread output data from the given {@link IOPort}.
   *
   * @param id The numeric ID of the {@link IOPort} to read from.
   */
  @GetMapping("/ports/{id}/output")
  public PortOutputResponsePacket getPort(@PathVariable("id") int id) {
    IOPort[] ports = vm.getPorts();
    if (id < 0 || id >= ports.length) {
      return null;
    }
    IOPort port = vm.getPorts()[id];

    return new PortOutputResponsePacket(port.readUnreadOutput());
  }

  /**
   * Provide input data to the given {@link IOPort}.
   *
   * @param id The numeric ID of the {@link IOPort} to write to.
   */
  @PostMapping("/ports/{id}/input")
  public void postPort(@RequestBody PortWriteRequestPacket packet, @PathVariable("id") int id,
      HttpServletResponse response) {
    IOPort[] ports = vm.getPorts();
    if (id < 0 || id >= ports.length) {
      response.setStatus(HttpStatus.NOT_FOUND.value());
      return;
    }
    IOPort port = vm.getPorts()[id];

    port.writeInput(packet.data());
    response.setStatus(HttpStatus.OK.value());
  }
}
