package uk.iatom.iAtomSys.client.shell;

import java.awt.Point;

public class ANSICodes {

  public static final String NEW_BUFFER = "\033[?1049h";
  public static final String OLD_BUFFER = "\033[?1049l";
  /**
   * Go home, ANSI, you're drunk...
   */
  public static final String YOU_ARE_DRUNK = "\033[H";

  public static final String CLEAR_SCREEN = "\033[2J";
  public static final String CLEAR_RIGHT_OF_CURSOR = "\033[0K";
  public static final String CLEAR_LEFT_OF_CURSOR = "\033[1K";
  public static final String CLEAR_LINE = "\033[2K";

  public static final String PUSH_CURSOR_POS = "\033[s";
  public static final String POP_CURSOR_POS = "\033[u";


  public static String getExitMetaSequence() {
    return String.join("", new String[]{
        "Issuing ANSI exit meta-sequence...",
        ANSICodes.OLD_BUFFER,
        "ANSI exit meta-sequence issued.",
    });
  }

  public static String moveTo(Point point) {
    // Row, then column
    return "\033[%d;%dH".formatted(point.y, point.x);
  }

  public static String moveToColumn(int column) {
    return "\033[%dG".formatted(column);
  }

  public static String moveUp(int n) {
    return "\033[%dA".formatted(n);
  }

  public static String moveDown(int n) {
    return "\033[%dB".formatted(n);
  }

  public static String moveRight(int n) {
    return "\033[%dC".formatted(n);
  }

  public static String moveLeft(int n) {
    return "\033[%dD".formatted(n);
  }

}
