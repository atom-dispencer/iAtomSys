package uk.iatom.iAtomSys.server.net;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.iatom.iAtomSys.common.Int16Helper;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;
import uk.iatom.iAtomSys.common.api.RunRequestPacket;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.VmStatus;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.server.IAtomSysVM;

@RestController()
@RequestMapping(value = "/command")
@ResponseBody
@ResponseStatus(HttpStatus.OK)
@SuppressWarnings({"unused"})
public class CommandRestController {

  private final Logger logger = LoggerFactory.getLogger(CommandRestController.class);

  public static final int INT16_HEX_LENGTH = 4;
  public static final String HELLO_WORLD = "World";
  public static final String ERR_NOT_ALLOWED_RUNNING = "Action not allowed: VM is running";

  public static String ERR_NUMBER_FORMAT(String addressStr) {
    return "Not a register or hex int-16: %s".formatted(addressStr);
  }

  public static String ERR_INPUT_LENGTH(String value) {
    return "Incorrect input length: %d!=%d on %s".formatted(value.length(), INT16_HEX_LENGTH, value);
  }

  public static String SET_SUCCESS(String addressStr, char address, String valueStr, char value) {
    return "Set %s (%04X) to %s (%04X)".formatted(addressStr, (int) address, valueStr, (int) value);
  }

  public static String DROP_DEBUG_SUCCESS(String name) {
    return "Dropped debug symbols: " + name;
  }


  private IAtomSysVM vm;

  @Autowired
  public CommandRestController(IAtomSysVM vm) {
    this.vm = vm;
  }

  @GetMapping("/hello")
  public String hello() {
    return HELLO_WORLD;
  }

  @PostMapping("/step")
  public String step(@RequestBody StepRequestPacket requestPacket) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return "Action not allowed: VM is running";
    }

    int count = requestPacket.count();

    logger.info("Stepping %d cycles".formatted(count));
    for (int i = 0; i < count; i++) {
      vm.processNextCycle();
    }

    String message = "Stepped %d cycles.".formatted(count);
    logger.info(message);
    return message;
  }

  //TODO Test, test, test!!

  /**
   * Load the image "name.img" to memory. If present, also load "name.img.json" as debug symbols.
   *
   * @param packet
   * @param response
   * @return
   */
  @PostMapping("/load_image")
  public String loadImage(@RequestBody LoadRequestPacket packet, HttpServletResponse response) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return "Action not allowed: VM is running";
    }

    String dirtyImageName =
        packet.imageName().endsWith(".img") ? packet.imageName() : packet.imageName() + ".img";
    File dirtyFile = new File(dirtyImageName);
    String cleanName = "images/" + dirtyFile.getName();
    File cleanFile = new File(cleanName);

    logger.info(
        "Attempting to load memorySlice image '%s' (%s)".formatted(cleanFile, dirtyFile));

    try (FileInputStream stream = new FileInputStream(cleanFile)) {
      int memorySize = vm.getMemory().getSize();

      long fileLength = cleanFile.length();
      if (fileLength > memorySize) {
        logger.error("File length %d > memorySlice size %d".formatted(fileLength, memorySize));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return "Requested file larger than VM memorySlice.";
      }

      byte[] byteArr = stream.readAllBytes();
      if (byteArr.length % 2 != 0) {
        logger.error(
            "File %s is invalid image: Odd length %d".formatted(cleanFile, byteArr.length));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return "Image is invalid.";
      }
      char[] buffer = new char[byteArr.length / 2];
      for (int i = 0; i < buffer.length; i++) {
        buffer[i] = Int16Helper.fromBytes(byteArr[2*i], byteArr[2*i + 1]);
      }

      if (buffer.length > memorySize) {
        logger.error("New buffer size %d > memorySlice size %d".formatted(fileLength, memorySize));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return "Requested file would overflow VM memorySlice.";
      } else if (buffer.length < memorySize) {
        logger.info("Inflating image from %d to %d bytes".formatted(buffer.length, memorySize));
        buffer = Arrays.copyOf(buffer, memorySize);
      }

      logger.info("Writing image... (%d bytes)".formatted(buffer.length));
      vm.getMemory().write((char) 0, buffer);
      vm.setStatus(VmStatus.PAUSED);

      File debugSymbolsFile = new File(cleanFile.getAbsolutePath() + ".json");
      try (FileInputStream fis = new FileInputStream(debugSymbolsFile)) {
        DebugSymbols debugSymbols = DebugSymbols.fromJson(debugSymbolsFile.getName(), fis);

        if (debugSymbols == null) {
          logger.warn("Debug symbols could not be parsed: {}", debugSymbolsFile.getAbsolutePath());
          response.setStatus(HttpStatus.OK.value());
          return "Loaded image but debug symbols unparsable";
        }

        vm.setDebugSymbols(debugSymbols);
      } catch (FileNotFoundException fnfx) {
        logger.warn("No debug symbols found: {}", fnfx.getMessage());
        response.setStatus(HttpStatus.OK.value());
        return "Loaded image but debug symbols not found";
      }

    } catch (IOException ioException) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return "Error reading file '%s'".formatted(cleanName);
    }

    logger.info("Finished loading new memorySlice image!");

    response.setStatus(HttpStatus.OK.value());
    return "Loaded %s".formatted(cleanFile);
  }

  @PostMapping("/set")
  public String set(@RequestBody SetRequestPacket request) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return ERR_NOT_ALLOWED_RUNNING;
    }

    String addressStr = request.address().toUpperCase().trim();
    String valueStr = request.value().toUpperCase().trim();

    // Parse address
    char address = 0;
    try {

      boolean isRegister = false;
      for (Register register : vm.getRegisterSet().getActiveRegisters()) {
        if (register.getName().equals(addressStr)) {
          isRegister = true;
          address = register.getAddress();
        }
      }

      if (!isRegister) {
        if (addressStr.length() != INT16_HEX_LENGTH) {
          return ERR_INPUT_LENGTH(addressStr);
        }

        address = Int16Helper.hexToInt16(addressStr);
      }

    } catch (NumberFormatException nfx) {
      return ERR_NUMBER_FORMAT(addressStr);
    }

    // Parse value
    char value = 0;
    try {

      boolean isRegister = false;
      for (Register register : vm.getRegisterSet().getActiveRegisters()) {
        if (register.getName().equals(valueStr)) {
          isRegister = true;
          value = register.get();
        }
      }

      if (!isRegister) {
        if (valueStr.length() != INT16_HEX_LENGTH) {
          return ERR_INPUT_LENGTH(valueStr);
        }

        value = Int16Helper.hexToInt16(valueStr);
      }
    } catch (NumberFormatException nfx) {
      return ERR_NUMBER_FORMAT(valueStr);
    }

    // Write value and finish up
    vm.getMemory().write(address, value);

    String message = SET_SUCCESS(addressStr, address, valueStr, value);
    logger.info(message);
    return message;
  }

  @PostMapping("/drop_debug")
  public String dropDebug() {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return ERR_NOT_ALLOWED_RUNNING;
    }

    String name = vm.getDebugSymbols().sourceName();
    if (name == null || name.isBlank()) {
      name = DebugSymbols.EMPTY_NAME;
    }

    vm.setDebugSymbols(DebugSymbols.empty());
    return DROP_DEBUG_SUCCESS(name);
  }

  @PostMapping("/run")
  public String run(@RequestBody RunRequestPacket packet) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return "VM is already running";
    }

    String startAddressStr = packet.startAddress();

    char startAddress = 0;

    boolean runFromHere;

    // Start string -> int
    try {
      if (startAddressStr.equals("here")) {
        runFromHere = true;
      } else {
        runFromHere = false;
        startAddress = Int16Helper.hexToInt16(startAddressStr);
      }
    } catch (NumberFormatException nfe) {
      return "Input must be a hex integer. Got %s.".formatted(startAddressStr);
    }

    // Determine real addresses
    if (!runFromHere) {
      vm.getRegisterSet().getRegister("PCR").set(startAddress);
    }

    char runningFrom = vm.getRegisterSet().getRegister("PCR").get();
    vm.runAsync();
    return "Running from %04X".formatted((int) runningFrom);
  }

  @PostMapping("/pause")
  public String pause() {
    if (vm.getStatus() != VmStatus.RUNNING) {
      return "Action not allowed: VM is not running";
    }

    vm.setStatus(VmStatus.PAUSED);

    char pcr = vm.getRegisterSet().getRegister("PCR").get();
    return "Paused at PCR: %04X".formatted((int) pcr);
  }
}
