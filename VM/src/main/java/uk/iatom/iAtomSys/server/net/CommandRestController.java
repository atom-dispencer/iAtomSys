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
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.server.IAtomSysVM;

@RestController()
@RequestMapping(value = "/command")
@ResponseBody
@ResponseStatus(HttpStatus.OK)
@SuppressWarnings({"unused"})
public class CommandRestController {

  private final Logger logger = LoggerFactory.getLogger(CommandRestController.class);

  @Autowired
  private IAtomSysVM vm;

  @GetMapping("/hello")
  public String hello() {
    return "World";
  }

  @PostMapping("/step")
  public String step(@RequestBody StepRequestPacket requestPacket) {
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

      ShortBuffer shortBuffer = ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN)
          .asShortBuffer();
      short[] buffer = new short[shortBuffer.capacity()];
      shortBuffer.get(buffer);

      if (buffer.length > memorySize) {
        logger.error("New buffer size %d > memorySlice size %d".formatted(fileLength, memorySize));
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return "Requested file would overflow VM memorySlice.";
      } else if (buffer.length < memorySize) {
        logger.info("Inflating image from %d to %d bytes".formatted(buffer.length, memorySize));
        buffer = Arrays.copyOf(buffer, memorySize);
      }

      logger.info("Writing image... (%d bytes)".formatted(buffer.length));
      vm.getMemory().write((short) 0, buffer);

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
    String addressStr = request.address();
    String valueStr = request.value();

    // Parse address
    short address;
    try {
      address = Short.parseShort(addressStr, 16);
    } catch (NumberFormatException nfx) {
      return "Not a hex int-16: %s".formatted(addressStr);
    }

    // Parse value
    short value;
    try {
      value = Short.parseShort(valueStr, 16);
    } catch (NumberFormatException nfx) {
      return "Not a hex int-16: %s".formatted(valueStr);
    }

    // Write value and finish up
    vm.getMemory().write(address, value);

    String message = "Set %s to %s.".formatted(addressStr, valueStr);
    logger.info(message);
    return message;
  }

  @PostMapping("/drop_debug")
  public String dropDebug() {
    String name = vm.getDebugSymbols().sourceName();
    vm.setDebugSymbols(DebugSymbols.empty());
    return "Dropped debug symbols: " + name;
  }
}
