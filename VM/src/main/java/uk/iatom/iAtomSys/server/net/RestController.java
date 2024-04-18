package uk.iatom.iAtomSys.server.net;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.iatom.iAtomSys.api.VMStatePacket;
import uk.iatom.iAtomSys.server.vm.IAtomSysVM;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("")
public class RestController {

  Logger logger = LoggerFactory.getLogger(RestController.class);

  @Autowired
  private IAtomSysVM vm;

  @GetMapping("/state")
  public VMStatePacket state(@RequestParam int memoryByteCount, HttpServletResponse response) {

    if (memoryByteCount < 1 || memoryByteCount > 512) {
      logger.warn("Refusing to fulfill 'state' request for %d memory bytes.".formatted(memoryByteCount));
      response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
      return null;
    }

    int startAddress = vm.getRegisterSet().ProgramCounter().get();
    byte[] memory = vm.getMemory().read(startAddress, memoryByteCount);

    RegisterSet regs = vm.getRegisterSet();
    Map<String, Integer> registers = Map.of( //
        "PCR", regs.ProgramCounter().get(), //
        "ACC", regs.Accumulator().get(), //
        "RTN", regs.Return().get(), //
        "STK", regs.IOStack().get(), //
        "MEM", regs.IOMemory().get(), //
        "PTR", regs.Pointer().get(), //
        "NUM", regs.Numeric().get(), //
        "IDK", regs.Idk().get(), //
        "FLG", regs.Flags().get() //
    );

    return new VMStatePacket(memory, registers);
  }

  @GetMapping("/hello")
  public String hello() {
    return "World";
  }

  @GetMapping("/step")
  public String step(@RequestParam int count) {
    if (count < 1 || count > 256) {
      logger.warn("Rejected step requesting %d cycles".formatted(count));
      return "Count must be in the interval [1,256]";
    }

    logger.info("Stepping %d cycles".formatted(count));
    for (int i = 0; i < count; i++) {
      vm.processNextCycle();
    }
    return "Stepped through %d cycles.".formatted(count);
  }

  //TODO Test, test, test!!
  @GetMapping("/loadmem")
  public String loadmem(@RequestParam String file, HttpServletResponse response) {
    String decodedFile = URLDecoder.decode(file, StandardCharsets.UTF_8);
    File dirtyFile = new File(decodedFile);
    String name = "loadmem/" + dirtyFile.getName();
    File sanitisedNameOnlyFile = new File(name);

    logger.info(
        "Attempting to load memory image '%s' (%s)".formatted(sanitisedNameOnlyFile, dirtyFile));

    try (FileInputStream stream = new FileInputStream(sanitisedNameOnlyFile)) {
      int memorySize = vm.getMemory().getSize();

      long fileLength = sanitisedNameOnlyFile.length();
      if (fileLength > memorySize) {
        logger.error("File length %d > memory size %d".formatted(fileLength, memorySize));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return "Requested file larger than VM memory.";
      }

      byte[] buffer = stream.readAllBytes();

      if (buffer.length > memorySize) {
        logger.error("New buffer size %d > memory size %d".formatted(fileLength, memorySize));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return "Requested file would overflow VM memory.";
      } else if (buffer.length < memorySize) {
        logger.info("Inflating image from %d to %d bytes".formatted(buffer.length, memorySize));
        buffer = Arrays.copyOf(buffer, memorySize);
      }

      logger.info("Writing image... (%d bytes)".formatted(buffer.length));
      vm.getMemory().write(0, buffer);

    } catch (IOException ioException) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return "Cannot access file.";
    }

    logger.info("Finished loading new memory image!");

    response.setStatus(HttpStatus.OK.value());
    return "Loaded.";
  }

}
