package uk.iatom.iAtomSys.client;

import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.Getter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.IAtomSysApplication;
import uk.iatom.iAtomSys.common.api.RegisterPacket;

@Component
public class ShellDisplay {

  public static final int COMMAND_MAX_WIDTH = 64;
  private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);
  private final AtomicBoolean alive = new AtomicBoolean(false);
  int COMMAND_TRAY_HEIGHT = 4;
  @Getter
  @Autowired
  private ShellDisplayState displayState;
  private PrintStream sysOutCache = System.out;
  private Terminal terminal;
  private final Supplier<Rectangle> BORDER_RECT = () -> {
    int start = 2;
    Point origin = new Point(start, start);
    Dimension bounds = new Dimension(
        terminal.getSize().getColumns() - start,
        terminal.getSize().getRows() - start
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
  private final Supplier<Rectangle> MEMORY_RUNNING_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();

    int width = (int) Math.floor(content.width * 0.375);

    Point origin = content.getLocation();
    Dimension dimension = new Dimension(
        width,
        content.height
    );
    return new Rectangle(origin, dimension);
  };
  private final Supplier<Rectangle> REGISTERS_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();

    int REGISTER_FLAG_WIDTH = 32;

    Point origin = new Point(
        (int) content.getMaxX() - REGISTER_FLAG_WIDTH,
        content.getLocation().y);
    Dimension dim = new Dimension(
        REGISTER_FLAG_WIDTH,
        Math.floorDiv(content.height, 2) + 1);

    return new Rectangle(origin, dim);
  };
  private final Supplier<Rectangle> FLAGS_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();
    Rectangle registers = REGISTERS_RECT.get();

    Point origin = new Point(
        registers.x,
        (int) registers.getMaxY() - 1
    );
    Dimension dim = new Dimension(
        registers.width,
        content.height - registers.height + 1
    );

    return new Rectangle(origin, dim);
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

  public boolean isAlive() {
    return alive.get();
  }

  public void activate() {
    logger.info("Activating ShellDisplay...");

    if (isAlive()) {
      return;
    }
    this.alive.set(true);

    try {
      terminal = TerminalBuilder.terminal();
    } catch (IOException iox) {
      logger.error("Error activating ShellDisplay. Could not create new Terminal. Cannot continue.",
          iox);
      System.exit(-1);
    }

    logger.info("Terminal size is: %s".formatted(terminal.getSize()));
    if (terminal.getSize().getRows() == 0 || terminal.getSize().getColumns() == 0) {
      logger.error("A terminal dimension is zero. Rows:%d; Columns:%d;".formatted(
              terminal.getSize().getRows(), terminal.getSize().getColumns()),
          new ShellDrawingException("Neither terminal dimension may be zero."));
    }

    print(ANSICodes.NEW_BUFFER, ANSICodes.YOU_ARE_DRUNK);

    displayState.update();
    displayState.setCommandMessage("Enter a command below to get started!");
    draw(true);

    System.out.println("If you can see this, System.out has not been disabled.");
  }

  @PreDestroy
  public void deactivate() {
    if (!isAlive()) {
      return;
    }
    // Henceforth, 'print' cannot be used.
    this.alive.set(false);

    // Enable System.out for logging
    enableSysOut();
    if (logger != null) {
      logger.info("Deactivating ShellDisplay...");
    } else {
      System.out.println("Deactivating ShellDisplay...");
    }

    try {
      terminal.close();
    } catch (IOException iox) {

      if (logger != null) {
        logger.error("Error closing old terminal.", iox);
      } else {
        System.err.println("Error closing old terminal." + iox);
      }

    } finally {
      // Try to restore the terminal's pre-application state
      System.out.println(ANSICodes.OLD_BUFFER + "\nExiting app...\n\n");

      // Eat any remaining input, so it doesn't mess with the parent terminal
      try {
        int ignored = System.in.read(new byte[System.in.available()]);
      } catch (IOException ignored) {
        // If it fails, oh well...
      }
    }
  }

  /**
   * The final displaying method to which other 'print' methods ultimately defer. Enables
   * {@link System#out}, writes to it, flushes the stream, and disables it again. This method is not
   * thread safe.
   *
   * @param messages The messages to display, which will be joined together with no delimiter.
   */
  private void print(@NotNull final String... messages) {
    if (!isAlive()) {
      logger.error("Cannot draw while ShellDisplay is not alive");
      return;
    }

    enableSysOut();
    String joined = String.join("", messages);
    System.out.print(joined);
    System.out.flush();
    disableSysOut();
  }

  /**
   * Display an ASCII-art box with the given boundaries, with a frame made from the given character.
   * The display character will fill the starting point - i.e. the frame is *inclusive* of its
   * boundaries.
   *
   * @param clearInside Whether to replace characters inside the frame with spaces.
   */
  private void printBox(Rectangle bounds, char c, boolean clearInside) {

    char[] headerFooter = new char[bounds.width];
    Arrays.fill(headerFooter, c);

    String middleLine;
    if (clearInside) {
      middleLine = c + " ".repeat(bounds.width - 2) + c;
    } else {
      middleLine = c + ANSICodes.moveRight(bounds.width - 2) + c;
    }

    String middleBlock = (ANSICodes.moveRight(bounds.x - 1) + middleLine + "\n")
        .repeat(bounds.height - 2);

    print(
        ANSICodes.PUSH_CURSOR_POS, //
        ANSICodes.moveTo(bounds.getLocation()), //
        new String(headerFooter) + "\n", //
        middleBlock, //
        ANSICodes.moveRight(bounds.x - 1) + new String(headerFooter), //
        ANSICodes.POP_CURSOR_POS
    );
  }

  private String formatParagraph(@Nullable Point start, boolean centre, int width,
      List<String> lines) {
    StringBuilder builder = new StringBuilder();

    if (centre) {
      if (width == 0) {
        width = Collections.max(lines, Comparator.comparing(String::length)).length();
      } else {

        // Some of the lines may overrun if the width is arbitrary
        // Long lines will get wrapped
        List<String> newLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
          String line = lines.get(i);
          if (line.length() > width) {
            newLines.add(i, line.substring(0, width));
            lines.add(i + 1, line.substring(width));
          } else {
            newLines.add(line);
          }
        }

        lines = newLines;
      }
      final int lambdaWidth = width;

      // Pad each line
      lines = lines.stream()
          .map(line -> " ".repeat(Math.max(0, (lambdaWidth - line.length()) / 2)) + line)
          .toList();
    }

    if (start != null) {
      builder.append(ANSICodes.moveTo(start));
    }

    for (String line : lines) {
      if (line == null) {
        line = "";
      }

      builder.append(line);
      builder.append(ANSICodes.moveDown(1));
      if (!line.isEmpty()) {
        builder.append(ANSICodes.moveLeft(line.length()));
      }
    }

    return builder.toString();
  }

  private void enableSysOut() {
    if (sysOutCache != null) {
      System.setOut(sysOutCache);
    }
  }

  private void disableSysOut() {
    if (sysOutCache == null) {
      sysOutCache = System.out;
    }
    System.setOut(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        // Do nothing, stream is dead.
      }
    }));
  }

  /**
   * Immediately redraw the UI with the current {@link ShellDisplayState}.
   */
  public void draw(boolean clearAll) {
    if (!isAlive()) {
      logger.error("Cannot draw while ShellDisplay not alive.");
      return;
    }

    long start = System.nanoTime();

    if (clearAll) {
      print(ANSICodes.CLEAR_SCREEN);
    }

    // TODO Funny edge case where the VM finishes executing super fast,
    //  so the state is PAUSED and the ShellCommands updater says to not clear the command,
    //  but the command is already cleared because of the run command, so most of the screen
    //  doesn't get cleared and stuff gets left on there...
    // TODO Also need to see why the VM seems to be reading infinite.img as ffa8, but NP++ hex
    //  says it should be 01a8...
    drawBackground();

    try {
      switch (displayState.getStatus()) {
        case STOPPED:
          drawStoppedMessage();
          break;
        case PAUSED:
          drawMemoryState();
          drawRegisters();
          drawFlags();
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
    printBox(CONTENT_RECT.get(), '+', true);

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

    print(
        ANSICodes.PUSH_CURSOR_POS,
        ANSICodes.moveTo(start),
        ANSICodes.moveDown(2),
        ANSICodes.moveRight(3),
        formatParagraph(null, false, 0, title),
        formatParagraph(null, true, titleWidth, lines),
        ANSICodes.POP_CURSOR_POS
    );

  }

  /**
   * Draw the background of the GUI, including the title and frame, without clearing the insides.
   */
  public void drawBackground() {
    int preHeadingWidth = 10;

    Rectangle content = CONTENT_RECT.get();
    printBox(content, ' ', true);

    Rectangle rect = BORDER_RECT.get();
    printBox(rect, '#', false);

    String title = " iAtomSysVM (%s) ".formatted(displayState.getStatus().name());

    print( //
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

    printBox(rect, '~', resetCommand);

    print( //
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

    print(
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
    printBox(bounds, '+', true);

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
        lineBuilder.append(" $ BRK $ ");
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

    print( //
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
    printBox(bounds, '+', true);

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

    print( //
        ANSICodes.PUSH_CURSOR_POS, //

        // Window
        ANSICodes.moveTo(start), //
        ANSICodes.moveRight(titleStart), //
        windowTitle,

        // Contents
        ANSICodes.moveTo(start), //
        ANSICodes.moveDown(2), //
        ANSICodes.moveRight(3), //
        formatParagraph(null, false, 0, title), //
        formatParagraph(null, true, titleWidth, lines), //

        //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  /**
   * Draw the values of the VM registers, including their names, addresses and values.
   */
  public void drawRegisters() {
    Rectangle rect = REGISTERS_RECT.get();
    printBox(rect, '+', true);

    String title = " Registers & Flags ";

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

    print( //
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

  public void drawFlags() {
    Rectangle rect = FLAGS_RECT.get();
    printBox(rect, '+', true);
  }
}
