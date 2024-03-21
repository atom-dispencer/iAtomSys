package uk.iatom.iAtomSys.client.shell;

import java.awt.*;

public class ANSICodes {

    public static final String NEW_BUFFER = "\033[?1049h";
    public static final String OLD_BUFFER = "\033[?1049l";
    /**
     * Go home, ANSI, you're drunk...
     */
    public static final String YOU_ARE_DRUNK = "\033[H";

    public static final String CLEAR_RIGHT_OF_CURSOR = "\033[0K";
    public static final String CLEAR_LEFT_OF_CURSOR = "\033[1K";
    public static final String CLEAR_LINE = "\033[2K";

    public static final String PUSH_CURSOR_POS = "\033[s";
    public static final String POP_CURSOR_POS = "\033[u";


    public static String getAbortSequence() {
        return String.join("", new String[]{
                ANSICodes.OLD_BUFFER,
                " ### ABORT ABORT ABORT ### "
        });
    }

    public static String moveCursor(Point point) {
        // Row, then column
        return "\033[%d;%dH".formatted(point.y, point.x);
    }

    public static String moveCursorHorizontalAbsolute(int column) {
        return "\033[%dG".formatted(column);
    }

}
