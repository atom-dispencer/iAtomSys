package uk.iatom.iAtomSys.server.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;
import uk.iatom.iAtomSys.common.api.VMStateRequestPacket;
import uk.iatom.iAtomSys.common.api.VMStateResponsePacket;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.server.device.IOPort;

@RestController
@RequestMapping("")
public class DefaultRestController {

  private final Logger logger = LoggerFactory.getLogger(DefaultRestController.class);

  @Autowired
  private IAtomSysVM vm;

  @GetMapping("/hello")
  public String hello() {
    return "World";
  }

  @PostMapping("/state")
  public VMStateResponsePacket state(@RequestBody VMStateRequestPacket packet) {

    // Collect the names of available memory images
    File imagesDirectory = new File("images/");
    String[] imageFileNames = imagesDirectory.list((file, name) -> name.endsWith(".img"));
    List<String> imageNames = new ArrayList<>();
    if (imageFileNames != null) {
      imageNames = Arrays.stream(imageFileNames).map((name) -> name.substring(0, name.length() - 5))
          .toList();
    }

    // Prepare memory region
    short pcr = Register.PCR(vm.getRegisterSet()).get();
    int width = Math.max(0, packet.sliceWidth());

    int startAddress = Math.max(0, pcr + packet.pcrOffset());
    int endAddress = Math.min(Short.MAX_VALUE, startAddress + width);
    short clampedStartAddress = (short) Math.min(Short.MAX_VALUE, startAddress);
    short clampedEndAddress = (short) Math.max(endAddress, clampedStartAddress);

    short clampedWidth = (short) (clampedEndAddress - clampedStartAddress);

    short[] memory = new short[clampedWidth];
    vm.getMemory().readRange(clampedStartAddress, memory);

    // Prepare registers
    RegisterSet regs = vm.getRegisterSet();
    List<RegisterPacket> registers = List.of( //
        Register.PCR(regs).toPacket(),
        Register.FLG(regs).toPacket(),
        Register.ACC(regs).toPacket(),
        Register.IDK(regs).toPacket(),
        Register.TBH(regs).toPacket(),
        Register.LOL(regs).toPacket()
    );

    // Get port addresses, in order of port ID
    List<Short> orderedPortAddresses = Arrays.stream(vm.getPorts()).map(IOPort::getAddress)
        .toList();

    return new VMStateResponsePacket(imageNames, clampedStartAddress, memory, registers,
        orderedPortAddresses);
  }
}
