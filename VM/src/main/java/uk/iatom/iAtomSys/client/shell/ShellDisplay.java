package uk.iatom.iAtomSys.client.shell;

import org.jline.terminal.Size;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.function.Supplier;

public class ShellDisplay {

    private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);

    private boolean alive;

    private static final Size DEFAULT_SIZE = new Size(120, 30);
    private Size terminalSize = DEFAULT_SIZE;
    private final Supplier<Point> SHORT_MESSAGE_START = () -> new Point(5, terminalSize.getRows());

    public void activate() {
        if (alive) return;
        this.alive = true;

        logger.info("Activating ShellDisplay...");
        terminalSize = getTargetTerminalSize();
        // TODO If zero, error and exit
        logger.info("Terminal size is: %s".formatted(terminalSize));


        print(
                ANSICodes.NEW_BUFFER,
                ANSICodes.YOU_ARE_DRUNK
        );

        drawBorder();
    }

    public void deactivate() {
        if (!alive) return;
        this.alive = false;
        logger.info("Deactivating ShellDisplay...");

        print(ANSICodes.OLD_BUFFER);
    }


    private void assertShellLive() {
        assert alive : new IllegalStateException("ShellDisplay cannot take actions while the shell is not live.");
    }

    public void drawBorder() {
        int leader = 10;
        String heading = " iAtomSysVM ";
        String headerLine = " %s%s%s ".formatted(
                "#".repeat(leader),
                heading,
                "#".repeat(terminalSize.getColumns() - 2 - leader - heading.length())
        );
        String midLine = " #" + ANSICodes.moveCursorHorizontalAbsolute(terminalSize.getColumns() - 1) + "# \n";
        String footerLine = " " + "#".repeat(terminalSize.getColumns() - 2) + " ";

        print(
                ANSICodes.PUSH_CURSOR_POS,
                ANSICodes.YOU_ARE_DRUNK,
                ANSICodes.CLEAR_LINE,
                headerLine + "\n",
                (midLine + "\n").repeat(terminalSize.getRows() - 2),
                footerLine,
                ANSICodes.CLEAR_LINE,
                ANSICodes.POP_CURSOR_POS
        );
    }

    public void showShortMessage(String shortMessage) {
        assertShellLive();
        String messageLengthEnforced = shortMessage.substring(0, Math.min(64, shortMessage.length()));
        print(
                ANSICodes.moveCursor(SHORT_MESSAGE_START.get()),
                messageLengthEnforced
        );
    }

    private void print(String ... messages) {
        String joined = String.join("", messages);
        System.out.print(joined);
        System.out.flush();
    }

    public Size getTargetTerminalSize() {
        try {
            // Does the terminal need to be disposed of?
            return TerminalBuilder.terminal().getSize();
        } catch (IOException e) {
            logger.error("Error fetching terminal dimensions. Defaulting to %s".formatted(DEFAULT_SIZE.toString()), e);
            return DEFAULT_SIZE;
        }
    }
}
