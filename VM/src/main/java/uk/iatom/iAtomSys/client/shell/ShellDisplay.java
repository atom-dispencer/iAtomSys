package uk.iatom.iAtomSys.client.shell;

import jakarta.validation.constraints.NotNull;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;
import lombok.Getter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellDisplay {

  public static final int COMMAND_MAX_WIDTH = 64;

  private static final Size DEFAULT_SIZE = new Size(120, 30);
  private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);
  @Getter
  private final ShellDisplayState state = new ShellDisplayState();
  private final PrintStream sysOutCache = System.out;
  private boolean alive;
  private Terminal terminal;

  private final Supplier<Point> COMMAND_BOX_POS = () -> new Point(5,
      terminal.getSize().getRows() - 5);
  private final Supplier<Point> CREDITS_POS = () -> {
    Point startPoint = COMMAND_BOX_POS.get();
    startPoint.translate(COMMAND_MAX_WIDTH + 8, 0);
    return startPoint;
  };
  private final Supplier<Point> REGISTERS_POS = () -> new Point(5, 5);
  private final Supplier<Point> FLAGS_POS = () -> {
    Point commandBoxStart = COMMAND_BOX_POS.get();
    Point flagsStart = REGISTERS_POS.get();
    int remainingHeight = commandBoxStart.y - flagsStart.y;

    flagsStart.translate(0, (int) (remainingHeight * 0.5));
    return flagsStart;
  };
  private final Supplier<Point> MEMORY_POS = () -> new Point(
      CREDITS_POS.get().x,
      REGISTERS_POS.get().y
  );
  // TODO GUI for program counter and registers at a minimum!

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

  public void deactivate() {
    if (!alive) {
      return;
    }
    this.alive = false;
    logger.info("Deactivating ShellDisplay...");

    try {
      terminal.close();
    } catch (IOException iox) {
      logger.error("Error closing old terminal.", iox);
    } finally {
      print(
          ANSICodes.OLD_BUFFER,
          "\nExiting app...\n"
      );
      try {
        int ignored = System.in.read(new byte[System.in.available()]);
      } catch (IOException ignored) {
      }
      enableSysOut();
    }
  }

  private void print(@NotNull final String... messages) {
    enableSysOut();
    String joined = String.join("", messages);
    System.out.print(joined);
    System.out.flush();
    disableSysOut();
  }

  private void enableSysOut() {
    System.setOut(sysOutCache);
  }

  private void disableSysOut() {
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

    drawBackground();
//    drawRegisters(state.getRegisters());
//    drawFlags(state.getFlags());
//    drawMemoryState(state.getMemoryState());
    drawCredits();
    drawCommandInput(state.getCommandMessage());
  }

  public void drawBackground() {
    assertShellLive();
    int preHeadingWidth = 10;
    String preHeading = "#".repeat(preHeadingWidth);
    String heading = " iAtomSysVM ";
    int postHeadingWidth = terminal.getSize().getColumns() - 2 - preHeadingWidth - heading.length();
    String postHeading = "#".repeat(postHeadingWidth);

    String headerLine = " %s%s%s ".formatted(preHeading, heading, postHeading);
    String midLine =
        " #" + ANSICodes.moveToColumn(terminal.getSize().getColumns() - 1) + "# ";
    String footerLine = " " + "#".repeat(terminal.getSize().getColumns() - 2) + " ";

    print(ANSICodes.PUSH_CURSOR_POS, ANSICodes.YOU_ARE_DRUNK, ANSICodes.CLEAR_SCREEN,
        headerLine + "\n", (midLine + "\n").repeat(terminal.getSize().getRows() - 2), footerLine,
        ANSICodes.POP_CURSOR_POS);
  }

  /**
   * Reset the typing-cursor to the start of the command input box and clear the current command
   * input.
   */
  public void drawCommandInput(String commandMessage) {
    assertShellLive();
    Point startPoint = COMMAND_BOX_POS.get();

    int length = commandMessage.length();
    String lengthCorrectedMessage = commandMessage;
    if (length < COMMAND_MAX_WIDTH) {
      String format = "%1$-" + COMMAND_MAX_WIDTH + "s";
      lengthCorrectedMessage = format.formatted(commandMessage);
    } else if (length > COMMAND_MAX_WIDTH) {
      lengthCorrectedMessage = commandMessage.substring(0, COMMAND_MAX_WIDTH);
    }

    String line1 = "~".repeat(COMMAND_MAX_WIDTH + 6);
    String line2 = "~ " + lengthCorrectedMessage + "   ~";
    String line3 = "~ :>" + " ".repeat(COMMAND_MAX_WIDTH) + " ~";
    String line4 = "~".repeat(COMMAND_MAX_WIDTH + 6);

    print(ANSICodes.moveTo(startPoint),
        line1 + ANSICodes.moveLeft(COMMAND_MAX_WIDTH + 6) + ANSICodes.moveDown(1),
        line2 + ANSICodes.moveLeft(COMMAND_MAX_WIDTH + 6) + ANSICodes.moveDown(1),
        line3 + ANSICodes.moveLeft(COMMAND_MAX_WIDTH + 6) + ANSICodes.moveDown(1),
        line4,
        ANSICodes.moveTo(startPoint) + ANSICodes.moveRight(4) + ANSICodes.moveDown(
            2));
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

  public void drawMemoryState(byte[] memoryState) {
    // TODO Should drawMemoryState receive a byte[] or pre-processed String?

    // Draw outline box

    // If there is no state, draw the help message
    if (memoryState == null) {

    }
    // If there IS a state, draw it
    else {

    }
  }
}
