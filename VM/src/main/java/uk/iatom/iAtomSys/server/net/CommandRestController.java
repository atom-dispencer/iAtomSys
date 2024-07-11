package uk.iatom.iAtomSys.server.net;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
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
import uk.iatom.iAtomSys.common.AddressFormatException;
import uk.iatom.iAtomSys.common.AddressHelper;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;
import uk.iatom.iAtomSys.common.api.RunRequestPacket;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.ToggleBreakpointRequestPacket;
import uk.iatom.iAtomSys.common.api.VmStatus;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.server.IAtomSysVM;

@RestController()
@RequestMapping(value = "/command")
@ResponseBody
@ResponseStatus(HttpStatus.OK)
@SuppressWarnings({"unused"})
public class CommandRestController {

  // Errors
  public static final int INT16_HEX_LENGTH = 4;
  public static final Function<String, String> ERR_NUMBER_FORMAT = "Not a register or hex int-16: %s"::formatted;
  public static final String ERR_NOT_ALLOWED_VM_RUNNING = "Action not allowed: VM is running";
  public static final String ERR_NOT_ALLOWED_VM_NOT_RUNNING = "Action not allowed: VM is not running";
  public static final String ERR_LOAD_FILE_TOO_LARGE = "Requested file larger than VM memory.";
  public static final String ERR_LOAD_IMAGE_INVALID = "Image is invalid.";
  public static final String ERR_LOAD_FILE_WOULD_OVERFLOW = "Requested file would overflow VM memorySlice.";
  public static final Function<String, String> ERR_LOAD_READING_FILE = "Error reading file %s"::formatted;
  // Successes
  public static final String HELLO_WORLD = "World";
  public static final Function<Integer, String> TBREAK_ADDED = "Added breakpoint at %04X"::formatted;
  public static final Function<Integer, String> TBREAK_REMOVED = "Removed breakpoint at %04X"::formatted;
  public static final Function<Integer, String> STEP_SUCCESS = "Stepped %d cycles"::formatted;
  public static final String LOAD_SYMBOLS_UNPARSABLE = "Loaded image but debug symbols unparsable";
  public static final String LOAD_SYMBOLS_NOT_FOUND = "Loaded image but debug symbols not found";
  public static final Function<String, String> LOAD_SUCCESS = "Loaded %s"::formatted;
  private final Logger logger = LoggerFactory.getLogger(CommandRestController.class);
  private IAtomSysVM vm;

  @Autowired
  public CommandRestController(IAtomSysVM vm) {
    this.vm = vm;
  }

  public static String SET_SUCCESS(String addressStr, char address, String valueStr, char value) {
    return "Set %s (%04X) to %s (%04X)".formatted(addressStr, (int) address, valueStr, (int) value);
  }

  public static String DROP_DEBUG_SUCCESS(String name) {
    return "Dropped debug symbols: " + name;
  }

  protected char parseRegisterOrInt16(String value) throws AddressFormatException {
    if (value == null || value.isBlank()) {
      throw new AddressFormatException("%s is not a valid register or address.".formatted(value));
    }
    value = value.toUpperCase().trim();

    boolean isRegister = false;
    boolean isRegisterSelfReference = false;
    boolean isHex = false;
    if (value.matches("[A-Z]{3}\\*")) {
      isRegisterSelfReference = true;
    } else if (value.matches("[A-Z]{3}")) {
      isRegister = true;
    } else if (value.matches("[A-Z0-9]{1,4}")) {
      isHex = true;
    } else {
      throw new AddressFormatException(ERR_NUMBER_FORMAT.apply(value));
    }

    // Treat as a memory address in hex
    if (isHex) {
      int lenDif = INT16_HEX_LENGTH - value.length();
      if (lenDif > 0) {
        value = "0".repeat(lenDif) + value;
      }

      return AddressHelper.hexToInt16(value);
    }

    // Treat as a register name (or self-reference)
    if (isRegisterSelfReference) {
      // Strip the * off
      value = value.substring(0, 3);
    }
    for (Register register : vm.getRegisterSet().getActiveRegisters()) {
      if (register.getName().equals(value)) {
        return isRegisterSelfReference ? register.getAddress() : register.get();
      }
    }

    throw new AddressFormatException(ERR_NUMBER_FORMAT.apply(value));
  }

  @GetMapping("/hello")
  public String hello() {
    return HELLO_WORLD;
  }

  @PostMapping("/step")
  public String step(@RequestBody StepRequestPacket requestPacket) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return ERR_NOT_ALLOWED_VM_RUNNING;
    }

    int count = requestPacket.count();

    logger.info("Stepping %d cycles".formatted(count));
    for (int i = 0; i < count; i++) {
      vm.processNextCycle();
    }

    String message = STEP_SUCCESS.apply(count);
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
      return ERR_NOT_ALLOWED_VM_RUNNING;
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
        return ERR_LOAD_FILE_TOO_LARGE;
      }

      byte[] byteArr = stream.readAllBytes();
      if (byteArr.length % 2 != 0) {
        logger.error(
            "File %s is invalid image: Odd length %d".formatted(cleanFile, byteArr.length));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ERR_LOAD_IMAGE_INVALID;
      }
      char[] buffer = new char[byteArr.length / 2];
      for (int i = 0; i < buffer.length; i++) {
        buffer[i] = AddressHelper.fromBytes(byteArr[2 * i], byteArr[2 * i + 1]);
      }

      if (buffer.length > memorySize) {
        logger.error("New buffer size %d > memorySlice size %d".formatted(fileLength, memorySize));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ERR_LOAD_FILE_WOULD_OVERFLOW;
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
          return LOAD_SYMBOLS_UNPARSABLE;
        }

        vm.setDebugSymbols(debugSymbols);
      } catch (FileNotFoundException fnfx) {
        logger.warn("No debug symbols found: {}", fnfx.getMessage());
        response.setStatus(HttpStatus.OK.value());
        return LOAD_SYMBOLS_NOT_FOUND;
      }

    } catch (IOException ioException) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return ERR_LOAD_READING_FILE.apply(cleanName);
    }

    logger.info("Finished loading new memorySlice image!");

    response.setStatus(HttpStatus.OK.value());
    return LOAD_SUCCESS.apply(cleanFile.getName());
  }

  @PostMapping("/set")
  public String set(@RequestBody SetRequestPacket request) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return ERR_NOT_ALLOWED_VM_RUNNING;
    }

    String addressStr = request.address().toUpperCase().trim();
    String valueStr = request.value().toUpperCase().trim();

    // Parse address
    char address;
    try {
      address = parseRegisterOrInt16(addressStr);
    } catch (AddressFormatException nfx) {
      String message = ERR_NUMBER_FORMAT.apply(addressStr);
      logger.warn(message);
      return message;
    }

    // Parse value
    char value;
    try {
      value = parseRegisterOrInt16(valueStr);
    } catch (AddressFormatException nfx) {
      String message = ERR_NUMBER_FORMAT.apply(valueStr);
      logger.warn(message);
      return message;
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
      return ERR_NOT_ALLOWED_VM_RUNNING;
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
        startAddress = AddressHelper.hexToInt16(startAddressStr);
      }
    } catch (AddressFormatException nfe) {
      return ERR_NUMBER_FORMAT.apply(startAddressStr);
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
      return ERR_NOT_ALLOWED_VM_NOT_RUNNING;
    }

    vm.setStatus(VmStatus.PAUSED);

    char pcr = vm.getRegisterSet().getRegister("PCR").get();
    return "Paused at PCR: %04X".formatted((int) pcr);
  }

  @PostMapping("/tbreak")
  public String tbreak(ToggleBreakpointRequestPacket packet) {
    if (vm.getStatus() == VmStatus.RUNNING) {
      return ERR_NOT_ALLOWED_VM_RUNNING;
    }

    String address = packet.addressStr();

    char bp;
    try {
      bp = AddressHelper.hexToInt16(address);
    } catch (AddressFormatException nfx) {
      logger.error("Toggle breakpoint couldn't parse address %s".formatted(address));
      return ERR_NUMBER_FORMAT.apply(address);
    }

    if (!vm.getBreakpoints().contains(bp)) {
      vm.getBreakpoints().add(bp);
      return TBREAK_ADDED.apply((int) bp);
    } else {
      vm.getBreakpoints().remove(Character.valueOf(bp));
      return TBREAK_REMOVED.apply((int) bp);
    }
  }

}
