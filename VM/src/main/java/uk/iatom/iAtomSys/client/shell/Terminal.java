package uk.iatom.iAtomSys.client.shell;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class Terminal {

  private static final Logger logger = LoggerFactory.getLogger(Terminal.class);

  private final AtomicBoolean alive = new AtomicBoolean(false);
  private final Dimension size = new Dimension(50, 50);
  private PrintStream sysOutCache = System.out;

  public boolean isAlive() {
    return alive.get();
  }

  public Dimension updateDimensions() {
    // TODO Update dimensions with ANSI: https://github.com/remkop/picocli/issues/634
    return new Dimension(50, 50);
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


  public void activate() {
    logger.info("Activating ShellDisplay...");

    if (isAlive()) {
      return;
    }
    this.alive.set(true);

    logger.info("Terminal size is: %s".formatted(getSize()));
    if (getSize().height == 0 || getSize().width == 0) {
      logger.error("A dimension is zero. Rows:%d; Columns:%d;".formatted(
              getSize().height, getSize().width),
          new ShellDrawingException("Neither dimension may be zero."));
    }

    print(ANSICodes.NEW_BUFFER, ANSICodes.YOU_ARE_DRUNK);


    System.out.println("If you can see this, System.out has not been disabled.");
  }

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

    // Try to restore the s pre-application state
    System.out.println(ANSICodes.OLD_BUFFER + "\nExiting app...\n\n");

    // Eat any remaining input, so it doesn't mess with the parent terminal
    try {
      int ignored = System.in.read(new byte[System.in.available()]);
    } catch (IOException ignored) {
      // If it fails, oh well...
    }
  }

  /**
   * The final displaying method to which other 'print' methods ultimately defer. Enables
   * {@link System#out}, writes to it, flushes the stream, and disables it again. This method is not
   * thread safe.
   *
   * @param messages The messages to display, which will be joined together with no delimiter.
   */
  public void print(@NonNull final String... messages) {
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
  public void printBox(Rectangle bounds, char c, boolean clearInside) {

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

  public String formatParagraph(@Nullable Point start, boolean centre, int width,
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

}
