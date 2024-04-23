package uk.iatom.iAtomSys.client;

import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;

public class ShellDisplay {

  public static final int COMMAND_MAX_WIDTH = 64;

  private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);
  @Getter
  private final ShellDisplayState state = new ShellDisplayState();
  private PrintStream sysOutCache = System.out;
  private boolean alive;
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

  int COMMAND_TRAY_HEIGHT = 4;
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

  private final Supplier<Rectangle> MEMORY_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();

    Point origin = content.getLocation();
    Dimension dimension = new Dimension(
        60,
        content.height
    );
    return new Rectangle(origin, dimension);
  };

  private final Supplier<Rectangle> REGISTERS_RECT = () -> {
    Rectangle content = CONTENT_RECT.get();

    int REGISTER_FLAG_WIDTH = 40;

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

  public void activate() {
    if (alive) {
      return;
    }
    this.alive = true;

    try {
      terminal = TerminalBuilder.terminal();
    } catch (IOException iox) {
      logger.error("Error activating ShellDisplay. Could not create new Terminal. Cannot continue.",
          iox);
      System.exit(-1);
    }

    logger.info("Activating ShellDisplay...");
    logger.info("Terminal size is: %s".formatted(terminal.getSize()));
    if (terminal.getSize().getRows() == 0 || terminal.getSize().getColumns() == 0) {
      logger.error("A terminal dimension is zero. Rows:%d; Columns:%d;".formatted(
              terminal.getSize().getRows(), terminal.getSize().getColumns()),
          new ShellDrawingException("Neither terminal dimension may be zero."));
    }

    print(ANSICodes.NEW_BUFFER, ANSICodes.YOU_ARE_DRUNK);

    // disableSysOut();

    state.setCommandMessage("Enter a command below to get started!");
    draw();

    System.out.println("If you can see this, System.out has not been disabled.");
  }

  @PreDestroy
  public void deactivate() {
    if (!alive) {
      return;
    }
    this.alive = false;

    // Enable System.out for logging
    enableSysOut();
    if (logger != null)
      logger.info("Deactivating ShellDisplay...");
    else
      System.out.println("Deactivating ShellDisplay...");

    try {
      terminal.close();
    } catch (IOException iox) {

      if (logger != null)
        logger.error("Error closing old terminal.", iox);
      else
        System.err.println("Error closing old terminal." + iox);

    } finally {
      // Try to restore the terminal's pre-application state
      print(
          ANSICodes.OLD_BUFFER,
          "\nExiting app...\n\n"
      );

      // Re-enable System.out because #print disables it
      enableSysOut();

      // Eat any remaining input, so it doesn't mess with the parent terminal
      try {
        int ignored = System.in.read(new byte[System.in.available()]);
      } catch (IOException ignored) {
        // If it fails, oh well...
      }
    }
  }

  private void print(@NotNull final String... messages) {
    enableSysOut();
    String joined = String.join("", messages);
    System.out.print(joined);
    System.out.flush();
    disableSysOut();
  }

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

  private void assertShellLive() {
    assert alive : new IllegalStateException(
        "ShellDisplay cannot take actions while the shell is not live.");
  }

  /**
   * Immediately redraw the UI with the current {@link ShellDisplayState}.
   */
  public void draw() {
    assertShellLive();

    long start = System.nanoTime();

    drawBackground();
    drawMemoryState();
    drawRegisters();
    drawFlags();
    drawCredits();
    drawCommandInput();

    long elapsedNanos = System.nanoTime() - start;
    double elapsedMillis = (elapsedNanos / 1_000_000d);
    logger.info("Redraw took %.3fms".formatted(elapsedMillis));
  }

  public void drawBackground() {
    assertShellLive();
    int preHeadingWidth = 10;

    print(ANSICodes.CLEAR_SCREEN);

    Rectangle rect = BORDER_RECT.get();
    printBox(rect, '#', false);

    print( //
        ANSICodes.PUSH_CURSOR_POS, //
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(2 + preHeadingWidth), //
        " iAtomSysVM ", //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  /**
   * Reset the typing-cursor to the start of the command input box and clear the current command
   * input.
   */
  // TODO Make drawCommandInput use printBox
  public void drawCommandInput() {
    assertShellLive();
    Rectangle rect = COMMAND_RECT.get();

    String commandMessage = state.getCommandMessage();

    int length = commandMessage.length();
    String lengthCorrectedMessage = commandMessage;
    if (length < COMMAND_MAX_WIDTH) {
      String format = "%1$-" + COMMAND_MAX_WIDTH + "s";
      lengthCorrectedMessage = format.formatted(commandMessage);
    } else if (length > COMMAND_MAX_WIDTH) {
      lengthCorrectedMessage = commandMessage.substring(0, COMMAND_MAX_WIDTH);
    }

    printBox(rect, '~', true);

    print( //

        // Short message
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(2) + ANSICodes.moveDown(1), //
        lengthCorrectedMessage,

        // Command input, ending at the input box
        ANSICodes.moveTo(rect.getLocation()), //
        ANSICodes.moveRight(2) + ANSICodes.moveDown(2), //
        ":>" //
    );
  }

  public void drawCredits() {
    assertShellLive();
    Point startPoint = CREDITS_POS.get();

    // TODO Add license information to credits
    String line1 = "iAtomSysVM v0 - https://github.com/atom-dispencer/iAtomSys";
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

  public void drawMemoryState() {
    Rectangle bounds = MEMORY_RECT.get();
    printBox(bounds, '+', true);

    //
    // Draw the stuff that will always need to be drawn, ignoring state
    //
    String title = " Memory State ";
    int titleStart = Math.floorDiv(bounds.width, 2) - Math.floorDiv(title.length(), 2);

    StringBuilder contents = new StringBuilder();

    //
    // Draw the state if it exists
    //
    if (state.getInstructions() != null) {

      for (String[] arr : state.getInstructions()) {
        final int FRAGMENT_WIDTH = 6;
        final String FORMAT = "%-" + FRAGMENT_WIDTH + "s";
        final StringBuilder lineBuilder = new StringBuilder(arr.length);

        for (String s: arr) {
          String formatted = String.format(FORMAT, s).substring(0, FRAGMENT_WIDTH);
          lineBuilder.append(formatted).append("  ");
        }

        contents.append(lineBuilder).append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(lineBuilder.length()));
      }

    }
    //
    // Otherwise draw a help message
    //
    else {

    }

    print( //
        ANSICodes.PUSH_CURSOR_POS, //

        // Title
        ANSICodes.moveTo(bounds.getLocation()), //
        ANSICodes.moveRight(titleStart), //
        title, //

        // Boxes/memory addresses/whatever
        ANSICodes.moveTo(bounds.getLocation()), //
        ANSICodes.moveRight(3), //
        ANSICodes.moveDown(2), //
        contents.toString(), //

        //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  public void drawRegisters() {
    assertShellLive();

    Rectangle rect = REGISTERS_RECT.get();
    printBox(rect, '+', true);

    String title = " Registers & Flags ";

    StringBuilder info = new StringBuilder();

    if (state.getRegisters() == null) {
      // Draw defaults
    } else {

      String header = " ID  Reg  @Addr   Value";
      info.append(header).append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(header.length()));
      String dashes = " --  ---  -----   -----";
      info.append(dashes).append(ANSICodes.moveDown(1)).append(ANSICodes.moveLeft(dashes.length()));
      String format = "%3d  %-3s  %04X    %04X";

      // Get the list of registers and sort it by ID, ascending
      List<RegisterPacket> packets = state.getRegisters();
      packets.sort(Comparator.comparing(RegisterPacket::id));

      for (int i = 0; i < packets.size(); i++) {

        // Add a line break before the hidden registers
        if (i == 4) {
          info.append(ANSICodes.moveDown(1));
        }

        RegisterPacket register = packets.get(i);
        String content = format.formatted(register.id(), register.name(), register.address(), register.value());
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
        ANSICodes.moveRight(2), //
        ANSICodes.moveDown(2), //
        info.toString(), //
        ANSICodes.POP_CURSOR_POS //
    );
  }

  public void drawFlags() {
    assertShellLive();

    Rectangle rect = FLAGS_RECT.get();
    printBox(rect, '+', true);
  }
}
