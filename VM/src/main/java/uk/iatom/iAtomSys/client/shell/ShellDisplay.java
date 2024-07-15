package uk.iatom.iAtomSys.client.shell;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.IAtomSysApplication;
import uk.iatom.iAtomSys.common.api.PortPacket;
import uk.iatom.iAtomSys.common.api.RegisterPacket;
import uk.iatom.iAtomSys.common.instruction.FlagHelper;

@Component
public class ShellDisplay {

  public static final int COMMAND_MAX_WIDTH = 64;
  private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);
  int COMMAND_TRAY_HEIGHT = 4;
  @Getter
  @Autowired
  private ShellDisplayState displayState;
  public Terminal terminal;
  private final Supplier<Rectangle> BORDER_RECT = () -> {
    int start = 2;
    Point origin = new Point(start, start);
    Dimension bounds = new Dimension(
        terminal.getSize().width - start,
        terminal.getSize().height - start
    );
    return new Rectangle(origin, bounds);
  };
  private final Supplier<Rectangle> CONTENT_RECT = () -> {
    Rectangle bounds = BORDER_RECT.get();

    Point origin = new Point(
        bounds.getLocation().x + 3,
        bounds.getLocation().y + 2
    );
    Dimension dim = new Dimension(
        bounds.width - 6,
        bounds.height - COMMAND_TRAY_HEIGHT - 5
    );
    return new Rectangle(origin, dim);
  };
  private final Supplier<Rectangle> COMMAND_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();

    Point origin = new Point(
        content.x,
        (int) content.getMaxY() + 1
    );
    Dimension dim = new Dimension(
        COMMAND_MAX_WIDTH + 6,
        4
    );

    return new Rectangle(origin, dim);
  };
  private final Supplier<Point> CREDITS_POS = () -> {
    Point startPoint = COMMAND_RECT.get().getLocation();
    startPoint.translate(COMMAND_MAX_WIDTH + 8, 0);
    return startPoint;
  };
  private final Supplier<Rectangle> REGISTERS_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();

    int WIDTH = 32;

    Point origin = new Point(
        (int) content.getMaxX() - WIDTH,
        content.getLocation().y);
    Dimension dim = new Dimension(
        WIDTH,
        Math.floorDiv(content.height, 2) + 1);

    return new Rectangle(origin, dim);
  };
  private final Supplier<Rectangle> FLAGS_RECT = () -> {
    Rectangle registers = REGISTERS_RECT.get();

    int WIDTH = 32;

    Point origin = new Point(
        registers.x - WIDTH,
        registers.y);
    Dimension dim = new Dimension(
        WIDTH + 1,
        registers.height);

    return new Rectangle(origin, dim);
  };
  private final Supplier<Rectangle> BREAKPOINTS_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();
    Rectangle flags = FLAGS_RECT.get();
    Rectangle registers = REGISTERS_RECT.get();

    Point origin = new Point(
        flags.x,
        (int) flags.getMaxY() - 1
    );
    Dimension dim = new Dimension(
        flags.width + registers.width,
        content.height - flags.height + 1
    );

    return new Rectangle(origin, dim);
  };
  private final Supplier<Rectangle> MEMORY_RUNNING_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();
    Rectangle flags = FLAGS_RECT.get();

    Point origin = content.getLocation();
    Dimension dimension = new Dimension(
        flags.x - origin.x - 2,
        content.height
    );
    return new Rectangle(origin, dimension);
  };

  private static List<String> getTitle() {
    List<String> title = new ArrayList<>();
    title.add("  _         _                   _____        __      ____  __ ");
    title.add(" (_)   /\\  | |                 / ____|       \\ \\    / |  \\/  |");
    title.add("  _   /  \\ | |_ ___  _ __ ___ | (___  _   _ __\\ \\  / /| \\  / |");
    title.add(" | | / /\\ \\| __/ _ \\| '_ ` _ \\ \\___ \\| | | / __\\ \\/ / | |\\/| |");
    title.add(" | |/ ____ | || (_) | | | | | |____) | |_| \\__ \\\\  /  | |  | |");
    title.add(" |_/_/    \\_\\__\\___/|_| |_| |_|_____/ \\__, |___/ \\/   |_|  |_|");
    title.add("                                       __/ |                  ");
    title.add("                                      |___/                   ");
    return title;
  }

  @EventListener
  public void deferredStartupUpdate(ContextRefreshedEvent event) {
    logger.warn("Doing delayed startup update");
    displayState.update();
    draw(true);
  }

  public void start() {
    terminal = new Terminal();
    terminal.activate();

    displayState.update();
    displayState.setCommandMessage("Enter a command below to get started!");
    draw(true);
  }

  public void stop() {
    terminal.deactivate();
  }

  /**
   * Immediately redraw the UI with the current {@link ShellDisplayState}.
   */
  public void draw(boolean clearAll) {
    if (!terminal.isAlive()) {
      logger.error("Cannot draw while ShellDisplay terminal not alive.");
      return;
    }

    long start = System.nanoTime();

    if (clearAll) {
      terminal.print(ANSICodes.CLEAR_SCREEN);
    }

    drawBackground();

    try {
      switch (displayState.getStatus()) {
        case STOPPED:
          drawStoppedMessage();
          break;
        case PAUSED:
          drawMemoryState();
          drawRegisters();
          drawFlagsAndPorts();
          drawBreakpointBox();
          // TODO Display values in ports (near registers/flags?)
          break;
        case RUNNING:
          drawRunningData();
          break;
      }
    } catch (Exception e) {
      logger.error("Error during drawing!", e);
    }
    // RUNNING is the only state where the screen refreshes without the user issuing a command
    // Therefore, we must avoid overwriting any partially typed commands.
    drawCredits();
    drawCommandInput(clearAll);

    long elapsedNanos = System.nanoTime() - start;
    double elapsedMillis = (elapsedNanos / 1_000_000d);
    logger.info("Redraw took %.3fms".formatted(elapsedMillis));
  }

  private void drawStoppedMessage() {
    Rectangle bounds = CONTENT_RECT.get();
    Point start = bounds.getLocation();
    terminal.printBox(CONTENT_RECT.get(), '+', true);

    String[] availableImagesArray = displayState.getAvailableImages();
    List<String> availableImages = List.of(
        availableImagesArray == null ? new String[0] : availableImagesArray);

    List<String> title = getTitle();
    title.add("");
    int titleWidth = Collections.max(title, Comparator.comparing(String::length)).length();
    int titleHeight = title.size();

    List<String> lines = new ArrayList<>();
    lines.add("");
    lines.add("The Virtual Machine is stopped.");
    lines.add("");
    lines.add("");
    if (availableImages.isEmpty()) {
      lines.add("There are no memory images [.img] in ./images/");
    } else {
      lines.add("Available memory images:");

      // Magic numbers:
      // 4: Box frame and padding
      // 2: The final two lines with the 'load' command hint
      int maxImageDisplayCount = Math.max(0, bounds.height - 4 - titleHeight - lines.size() - 2);
      int imageDisplayCount = Math.min(availableImages.size(), maxImageDisplayCount);

      // Wrap in ArrayList to make it mutable
      List<String> toAdd = new ArrayList<>(availableImages.subList(0, imageDisplayCount));

      // If the desired number of entries would overflow the available lines, add a message saying
      // how many entries have been omitted
      if (imageDisplayCount < availableImages.size() && !toAdd.isEmpty()) {
        toAdd.remove(toAdd.size() - 1);
        toAdd.add("... and %d more.".formatted(availableImages.size() - imageDisplayCount));
      }

      lines.addAll(toAdd);

      lines.add("");
      lines.add("To load an image: load <image_name>");
    }

    List<String> tips = new ArrayList<>();
    tips.add("~~ Commands ~~");
    tips.add("");

    for (String tip : ShellCommands.HELP_PAGES) {
      int deficit = COMMAND_MAX_WIDTH - tip.length();

      String[] split = tip.split(":");
      if (split.length != 2) {
        logger.error("Bad formatting for help message: {}", tip);
        tips.add(tip);
      } else {
        tips.add(split[0] + " ".repeat(deficit + 1) + split[1]);
      }
    }

    tips.add("");
    tips.add("Arguments marked with ? are optional or have defaults.");
    tips.add("Further details on GitHub - link in the copyright notice.");

    Rectangle tipsBounds = new Rectangle(
        bounds.x + titleWidth + 4,
        bounds.y + (bounds.height - tips.size()) / 2,
        bounds.width - titleWidth - 4,
        bounds.height - 4
    );

    boolean tallEnough = tipsBounds.height > tips.size();
    boolean wideEnough = tipsBounds.width > COMMAND_MAX_WIDTH + 2;
    boolean showCommandTips = tallEnough && wideEnough;

    terminal.print(
        ANSICodes.PUSH_CURSOR_POS,
        ANSICodes.moveTo(start),
        ANSICodes.moveDown(2),
        ANSICodes.moveRight(3),
        terminal.formatParagraph(null, false, 0, title),
        terminal.formatParagraph(null, true, titleWidth, lines),
        showCommandTips ? terminal.formatParagraph(tipsBounds.getLocation(), true, tipsBounds.width, tips)
            : "",
        ANSICodes.POP_CURSOR_POS
    );

  }

  /**
   * Draw the background of the GUI, including the title and frame, without clearing the insides.
   */
  public void drawBackground() {
    int preHeadingWidth = 10;

    Rectangle content = CONTENT_RECT.get();
    terminal.printBox(content, ' ', true);

    Rectangle rect = BORDER_RECT.get();
    terminal.printBox(rect, '#', false);

    String title = " iAtomSysVM (%s) ".formatted(displayState.getStatus().name());

    terminal.print( //
        ANSICodes.PUSH_CURSOR_POS, //
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(2 + preHeadingWidth), //
        title, //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  /**
   * Reset the typing-cursor to the start of the command input box and clear the current command
   * input.
   */
  public void drawCommandInput(boolean resetCommand) {
    Rectangle rect = COMMAND_RECT.get();

    String commandMessage = displayState.getCommandMessage();

    int length = commandMessage.length();
    String lengthCorrectedMessage = commandMessage;
    if (length < COMMAND_MAX_WIDTH) {
      String format = "%1$-" + COMMAND_MAX_WIDTH + "s";
      lengthCorrectedMessage = format.formatted(commandMessage);
    } else if (length > COMMAND_MAX_WIDTH) {
      lengthCorrectedMessage = commandMessage.substring(0, COMMAND_MAX_WIDTH);
    }

    terminal.printBox(rect, '~', resetCommand);

    terminal.print( //
        resetCommand ? "" : ANSICodes.PUSH_CURSOR_POS, //

        // Short message
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(2) + ANSICodes.moveDown(1), //
        lengthCorrectedMessage,

        // Command input, ending at the input box
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(2) + ANSICodes.moveDown(2), //
        ":>", //

        resetCommand ? "" : ANSICodes.POP_CURSOR_POS //
    );
  }

  /**
   * Draw a cute little message so that everyone knows who made this <3. Oh, and the GitHub help
   * message as well.
   */
  public void drawCredits() {
    Point startPoint = CREDITS_POS.get();

    // TODO Add license information to credits
    String line1 = "iAtomSysVM %s - https://github.com/atom-dispencer/iAtomSys"
        .formatted(IAtomSysApplication.getCicdVersion());
    String line2 = "Copyright © Adam Spencer 2024";

    terminal.print(
        ANSICodes.PUSH_CURSOR_POS,
        ANSICodes.moveTo(startPoint),
        ANSICodes.moveDown(1),
        line1 + ANSICodes.moveLeft(line1.length()) + ANSICodes.moveDown(1),
        line2,
        ANSICodes.POP_CURSOR_POS
    );
  }

  /**
   * Draw the memorySlice state of the running VM, including breakpoints and the current PCR
   * position.
   */
  public void drawMemoryState() {
    Rectangle bounds = MEMORY_RUNNING_RECT.get();
    terminal.printBox(bounds, '+', true);

    //
    // Draw the stuff that will always need to be drawn, ignoring state
    //
    String title = " Memory State ";
    int titleStart = Math.floorDiv(bounds.width, 2) - Math.floorDiv(title.length(), 2);

    StringBuilder contents = new StringBuilder();

    // Get the value of the program counter
    char pcr = 0;
    boolean pcrKnown = false;
    for (RegisterPacket registerPacket : displayState.getRegisters()) {
      if (registerPacket.name().equals("PCR")) {
        pcr = registerPacket.value();
        pcrKnown = true;
        break;
      }
    }

    // Title for the disassembly
    StringBuilder titleBuilder = new StringBuilder();
    String TITLE = "Address  Hex  * Operands";
    String LINE = "________ ____ _ _____________";
    titleBuilder.append(TITLE).append(ANSICodes.moveDown(1))
        .append(ANSICodes.moveLeft(TITLE.length()));
    titleBuilder.append(LINE).append(ANSICodes.moveDown(1))
        .append(ANSICodes.moveLeft(LINE.length()));

    int widthPadding = 3;
    int slimColumnWidth = LINE.length();
    int paddedColumnWidth = slimColumnWidth + 2 * widthPadding;
    int columns = bounds.width / paddedColumnWidth;
    int availableRows = bounds.height - 6;
    int spareWidth = bounds.width - columns * paddedColumnWidth;

    List<String> formattedLines = new ArrayList<>();

    Map<Integer, String> reservedAddresses = displayState.getNamedAddresses();

    // Format each instruction line
    for (int i = 0; i < displayState.getDisassembly().size(); i++) {
      String[] arr = displayState.getDisassembly().get(i);

      final int FRAGMENT_WIDTH = 4;
      final String FORMAT = "%-" + FRAGMENT_WIDTH + "s";
      final StringBuilder lineBuilder = new StringBuilder(arr.length);

      // TODO Make sure this doesn't explode with wrapping around or whatever

      // Reserved addresses
      int address = displayState.getMemorySliceStartAddress() + i;
      if (reservedAddresses.containsKey(address)) {
        String name = reservedAddresses.get(address);
        name = name.length() <= 3 ? name : name.substring(0, 3);
        lineBuilder.append("< %-3s  > ".formatted(name));
      } else {
        lineBuilder.append("<0x%04x> ".formatted(address));
      }

      // Breakpoints
      if (List.of(displayState.getBreakpoints()).contains((char) address)) {
        formattedLines.add("$ BRKP $");
      }

      // The instruction itself
      lineBuilder.append("%04x ".formatted((int) displayState.getMemory()[i]));

      // Program counter pointer indicator
      if (!pcrKnown || address == pcr) {
        lineBuilder.append("* ");
      } else {
        lineBuilder.append("  ");
      }

      // Operands
      for (String s : arr) {
        String formatted = String.format(FORMAT, s).substring(0, FRAGMENT_WIDTH);
        lineBuilder.append(formatted).append(" ");
      }

      formattedLines.add(lineBuilder.toString());
    }

    // Create builders for columns
    StringBuilder[] columnBuilders = new StringBuilder[columns];
    for (int i = 0; i < columnBuilders.length; i++) {
      columnBuilders[i] = new StringBuilder();
    }
    // Build the lines into the columns
    for (int i = 0; i < formattedLines.size() && i < columns * availableRows; i++) {
      int column = Math.floorDiv(i, availableRows);
      StringBuilder columnBuilder = columnBuilders[column];

      if (columnBuilder.isEmpty()) {
        // Go to the starting location before drawing title
        Point start = new Point(bounds.getLocation().x, bounds.getLocation().y);
        start.translate(widthPadding + spareWidth / 2 + column * paddedColumnWidth, 2);
        columnBuilder.append(ANSICodes.moveTo(start));

        columnBuilder.append(titleBuilder);
        columnBuilders[column] = columnBuilder;
      }

      String line = formattedLines.get(i);
      columnBuilder.append(line);
      columnBuilder.append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(line.length()));
    }

    // String the columns together
    if (columnBuilders.length > 0) {
      for (int i = 0; i < columnBuilders.length; i++) {
        StringBuilder columnBuilder = columnBuilders[i];

        if (columnBuilder == null) {
          continue;
        }
        if (columnBuilder.toString().isEmpty()) {
          logger.warn("Empty builder for column %d".formatted(i));
          continue;
        }

        contents.append(columnBuilder);
      }
    } else {
      contents.append("Help!").append(ANSICodes.moveDown(2)).append(ANSICodes.moveLeft(5));
      contents.append("You're").append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(6));
      contents.append("squishing").append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(9));
      contents.append("me!").append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(9));
    }

    terminal.print( //
        ANSICodes.PUSH_CURSOR_POS, //

        // Title
        ANSICodes.moveTo(bounds.getLocation()), //
        ANSICodes.moveRight(titleStart), //
        title, //

        // Boxes/memorySlice addresses/whatever
        ANSICodes.moveTo(bounds.getLocation()), //
        ANSICodes.moveRight(3), //
        ANSICodes.moveDown(2), //
        contents.toString(), //

        //
        ANSICodes.POP_CURSOR_POS //
    );
  }


  /**
   * When the VM is running, display data about said runtime, such as times a breakpoint is hit or
   * uptime.
   */
  public void drawRunningData() {
    Rectangle bounds = CONTENT_RECT.get();
    Point start = bounds.getLocation();
    terminal.printBox(bounds, '+', true);

    String windowTitle = " VM is running... ";
    int titleStart = Math.floorDiv(bounds.width, 2) - Math.floorDiv(windowTitle.length(), 2);

    List<String> title = getTitle();
    title.add("");
    int titleWidth = Collections.max(title, Comparator.comparing(String::length)).length();

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss, dd LLL yyyy");
    LocalDateTime runningSince = displayState.getRunningSince();
    Duration runningTime = Duration.between(runningSince, LocalDateTime.now());

    // Don't worry about rounding here, it's just an estimate
    double executed = (double) displayState.getRunningInstructionsExecuted() / 1_000_000;
    long seconds = runningTime.getSeconds();
    double executionRate = seconds == 0 ? 0 : executed / seconds;

    List<String> lines = new ArrayList<>();
    lines.add("Up-time              : %02d:%02d:%02d".formatted(
        runningTime.toHoursPart(),
        runningTime.toMinutesPart(),
        runningTime.toSecondsPart()
    ));
    lines.add("Running Since        : %s".formatted(dateTimeFormatter.format(runningSince)));
    lines.add("Instructions Executed: %.2fM (%.2fM/s avg.)".formatted(executed, executionRate));
    lines.add("");
    lines.add("'help' : See help with commands");
    lines.add("'pause' : Pause the VM and inspect its state");

    terminal.print( //
        ANSICodes.PUSH_CURSOR_POS, //

        // Window
        ANSICodes.moveTo(start), //
        ANSICodes.moveRight(titleStart), //
        windowTitle,

        // Contents
        ANSICodes.moveTo(start), //
        ANSICodes.moveDown(2), //
        ANSICodes.moveRight(3), //
        terminal.formatParagraph(null, false, 0, title), //
        terminal.formatParagraph(null, true, titleWidth, lines), //

        //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  /**
   * Draw the values of the VM registers, including their names, addresses and values.
   */
  public void drawRegisters() {
    Rectangle rect = REGISTERS_RECT.get();
    terminal.printBox(rect, '+', true);

    String title = " Registers ";

    StringBuilder info = new StringBuilder();

    if (displayState.getRegisters() == null) {
      // Draw defaults
    } else {

      String header = " ID  Reg  *Addr   Value";
      info.append(header).append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(header.length()));
      String dashes = " --  ---  -----   -----";
      info.append(dashes).append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(dashes.length()));
      String format = "%3d  %-3s  %04X    %04X";

      // Get the list of registers and sort it by ID, ascending
      List<RegisterPacket> packets = new ArrayList<>(List.of(displayState.getRegisters()));
      packets.sort(Comparator.comparing(RegisterPacket::id));

      for (int i = 0; i < packets.size(); i++) {

        // Add a line break before the hidden registers
        if (i == 4) {
          info.append(ANSICodes.moveDown(1));
        }

        RegisterPacket register = packets.get(i);
        String content = format.formatted(register.id(), register.name(), (int) register.address(),
            (int) register.value());
        info.append(content);
        info.append(ANSICodes.moveLeft(content.length()));
        info.append(ANSICodes.moveDown(1));
      }
    }

    terminal.print( //
        ANSICodes.PUSH_CURSOR_POS, //
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(Math.floorDiv(rect.width, 2) - Math.floorDiv(title.length(), 2)), //
        title, //
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(4), //
        ANSICodes.moveDown(2), //
        info.toString(), //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  public void drawFlagsAndPorts() {
    Rectangle bounds = FLAGS_RECT.get();
    terminal.printBox(bounds, '+', true);

    RegisterPacket flags = null;
    for (RegisterPacket packet: displayState.getRegisters()) {
      if ("FLG".equals(packet.name())) {
        flags = packet;
        break;
      }
    }

    Point flagStart = new Point(bounds.x + 4, bounds.y + 2);

    List<String> flagLines = new ArrayList<>();
    String flagsTitle = " Flags ";
    flagLines.add(flagsTitle);
    flagLines.add("-------");

    if (flags == null) {
      flagLines.add("Port Addr Value");
      flagLines.add("---- ---- ------");
    } else {
      char flagsChar = flags.value();

      for (int i = 0; i < FlagHelper.FLAGS_COUNT; i++) {
        FlagHelper.Flag flag = FlagHelper.Flag.fromBitIndex(i);
        String name = flag.toString();
        boolean value = FlagHelper.getFlag(i, flagsChar);
        flagLines.add("%-6s%s".formatted(
            name,
            value ? '+' : '-')
        );
      }

    }

    Point portStart = new Point(flagStart.x + flagsTitle.length() + 2, flagStart.y);
    List<String> portLines = new ArrayList<>();
    portLines.add("Port Addr Value");
    portLines.add("---- ---- ------");

    if (displayState.getPorts().length == 0) {
      portLines.add("");
      portLines.add("No port data");
    } else{
      for (PortPacket port : displayState.getPorts()) {
        String name = "IO" + port.id();
        String address = "%04X".formatted((int) port.address());
        String value = "%04X".formatted((int) port.value());
        char character = port.value();

        // Remove control characters
        if (character < 32 || character == 127) {
          character = '?';
        }

        portLines.add("%-4s %4s %4s %1s".formatted(name, address, value, character));
      }
    }

    String title = " Flags & Ports ";
    terminal.print(
        ANSICodes.PUSH_CURSOR_POS,
        ANSICodes.moveTo(bounds.getLocation()),
        ANSICodes.moveRight((bounds.width - title.length()) / 2),
        title,
        terminal.formatParagraph(flagStart, false, 0, flagLines),
        terminal.formatParagraph(portStart, false, 0, portLines),
        ANSICodes.POP_CURSOR_POS
    );

  }

  public void drawBreakpointBox() {
    Rectangle bounds = BREAKPOINTS_RECT.get();
    terminal.printBox(bounds, '+', true);

    int padding = 2;
    Rectangle innerBounds = new Rectangle(bounds.x + padding, bounds.y + padding,
        bounds.width - 2 * padding, bounds.height - 2 * padding);

    char pcr = 0;
    for (RegisterPacket rp : displayState.getRegisters()) {
      if (rp.name().equals("PCR")) {
        pcr = rp.value();
      }
    }

    int nameWidth = bounds.width - 2 * padding - 6;

    List<String> lines = new ArrayList<>();

    if (displayState.getBreakpoints().length == 0) {
      lines.add("No breakpoints");
      lines.add("Go have a 'tbreak'");
    } else {
      List<Character> sortedBreakpoints = Arrays.stream(displayState.getBreakpoints()).sorted()
          .toList();
      int startIndex = Math.max(findClosestIndex(pcr, sortedBreakpoints) - 3, 0);
      int maxLines = innerBounds.height - 1;
      int count = Math.min(maxLines, sortedBreakpoints.size() - startIndex);

      lines.add("Viewing %d-%d of %d".formatted(
          startIndex + 1,
          startIndex + count,
          sortedBreakpoints.size()
      ));

      for (int j = startIndex; j < count; j++) {
        int address = (int) sortedBreakpoints.get(j);

        String debug = displayState.getDebugSymbols().functions().getOrDefault(address, null);
        if (debug == null) {
          debug = displayState.getDebugSymbols().comments().getOrDefault(address, null);
        }
        if (debug == null) {
          debug = displayState.getDebugSymbols().labels().getOrDefault(address, null);
        }
        if (debug == null) {
          debug = displayState.getNamedAddresses().getOrDefault(address, "(No info)");
        }

        if (debug.length() > nameWidth) {
          debug = debug.substring(0, nameWidth);
        } else if (debug.length() < nameWidth) {
          debug = debug + " ".repeat(nameWidth - debug.length());
        }
        lines.add("%04x: %s".formatted(address, debug));
      }
    }

    String title = " VM Breakpoints ";

    terminal.print(
        ANSICodes.PUSH_CURSOR_POS,
        ANSICodes.moveTo(bounds.getLocation()),
        ANSICodes.moveRight((bounds.width - title.length()) / 2),
        title,
        terminal.formatParagraph(innerBounds.getLocation(), true, innerBounds.width, lines),
        ANSICodes.POP_CURSOR_POS
    );
  }

  private int findClosestIndex(char target, List<Character> breakpoints) {

    if (breakpoints.isEmpty()) {
      throw new IllegalArgumentException("Empty breakpoint lists should be handled separately.");
    }

    for (int i = 0; i < breakpoints.size(); i++) {
      if (breakpoints.get(i) > target) {
        return i;
      }
    }

    return breakpoints.size() - 1;
  }
}
