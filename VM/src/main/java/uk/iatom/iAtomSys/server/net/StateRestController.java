package uk.iatom.iAtomSys.server.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.DebugSymbolsRequestPacket;
import uk.iatom.iAtomSys.common.api.RegisterPacket;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryResponsePacket;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.server.device.IOPort;

@RestController
@RequestMapping("/state")
public class StateRestController {

  private final Logger logger = LoggerFactory.getLogger(StateRestController.class);

  @Autowired
  private IAtomSysVM vm;

  @GetMapping("/images")
  public List<String> images() {

    File imagesDirectory = new File("images/");
    String[] imageFileNames = imagesDirectory.list((file, name) -> name.endsWith(".img"));

    if (imageFileNames == null) {
      return new ArrayList<>();
    }

    return Arrays.stream(imageFileNames).map((name) -> name.substring(0, name.length() - 5)).toList();
  }

  @GetMapping("/registers")
  public List<RegisterPacket> registers() {

    RegisterSet regs = vm.getRegisterSet();
    return List.of( //
        Register.PCR(regs).toPacket(),
        Register.FLG(regs).toPacket(),
        Register.ACC(regs).toPacket(),
        Register.IDK(regs).toPacket(),
        Register.TBH(regs).toPacket(),
        Register.LOL(regs).toPacket()
    );
  }

  @GetMapping("/ports")
  public List<Short> ports() {
    // Get port addresses, in order of port ID
    return Arrays.stream(vm.getPorts()).map(IOPort::getAddress).toList();
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

    // TODO StateRestController needs to generate/fetch correct DebugSymbols (from JSON? from file?)
    DebugSymbols currentDebugSymbols = vm.getDebugSymbols();
    return currentDebugSymbols.takeRelevant(packet.startAddress(), packet.endAddress());
  }
}
