package uk.iatom.iAtomSys.client.shell;

import jakarta.validation.constraints.NotNull;
import org.jline.terminal.Size;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;

public class ShellDisplay {

    private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);

    private boolean alive;

    private static final Size DEFAULT_SIZE = new Size(120, 30);
    private Size terminalSize = DEFAULT_SIZE;
    private final Supplier<Point> COMMAND_BOX_POS = () -> new Point(5, terminalSize.getRows() - 10);
    private final int COMMAND_MAX_WIDTH = 64;
    private final PrintStream sysOutCache = System.out;

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

        disableSysOut();
        draw();
        drawShortMessage("Enter a command below to get started!");
    }

    public void deactivate() {
        if (!alive) return;
        this.alive = false;
        logger.info("Deactivating ShellDisplay...");

        print(ANSICodes.OLD_BUFFER);
        enableSysOut();
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

    private void print(@NotNull final String... messages) {
        enableSysOut();
        String joined = String.join("", messages);
        System.out.print(joined);
        System.out.flush();
        disableSysOut();
    }

    public void disableSysOut() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // Do nothing, stream is dead.
            }
        }));
    }

    public void enableSysOut() {
        System.setOut(sysOutCache);
    }

    private void assertShellLive() {
        assert alive : new IllegalStateException("ShellDisplay cannot take actions while the shell is not live.");
    }

    public void onAnyCommand() {
        drawCommandInput();
    }

    public void draw() {
        assertShellLive();
        drawBorder();
        drawCommandInput();
    }

    public void drawBorder() {
        assertShellLive();
        int leader = 10;
        String heading = " iAtomSysVM ";
        String headerLine = " %s%s%s ".formatted(
                "#".repeat(leader),
                heading,
                "#".repeat(terminalSize.getColumns() - 2 - leader - heading.length())
        );
        String midLine = " #" + ANSICodes.moveCursorHorizontalAbsolute(terminalSize.getColumns() - 1) + "# ";
        String footerLine = " " + "#".repeat(terminalSize.getColumns() - 2) + " ";

        print(
                ANSICodes.PUSH_CURSOR_POS,
                ANSICodes.YOU_ARE_DRUNK,
                ANSICodes.CLEAR_LINE,
                headerLine + "\n",
                (midLine + "\n").repeat(terminalSize.getRows() - 2),
                footerLine,
                ANSICodes.POP_CURSOR_POS
        );
    }

    public void drawShortMessage(@NotNull final String shortMessage) {
        assertShellLive();

        int length = shortMessage.length();
        String lengthCorrectedMessage = shortMessage;
        if (length < COMMAND_MAX_WIDTH) {
            String format = "%1$-" + COMMAND_MAX_WIDTH + "s";
            lengthCorrectedMessage = format.formatted(shortMessage);
        } else if (length > COMMAND_MAX_WIDTH) {
            lengthCorrectedMessage = shortMessage.substring(0, COMMAND_MAX_WIDTH);
        }

        print(
                ANSICodes.PUSH_CURSOR_POS,
                ANSICodes.moveCursor(COMMAND_BOX_POS.get()),
                ANSICodes.moveCursorDown(1),
                ANSICodes.moveCursorRight(2),
                lengthCorrectedMessage,
                ANSICodes.POP_CURSOR_POS
        );
    }

    /**
     * Reset the typing-cursor to the start of the command input box and clear the current command input.
     */
    public void drawCommandInput() {
        //TODO should drawCommandInput clear the current command? Maybe need to flush System.in?
        assertShellLive();
        Point startPoint = COMMAND_BOX_POS.get();

        String line1 = "~".repeat(COMMAND_MAX_WIDTH + 6);
        String line2 = "~ " + ANSICodes.moveCursorRight(COMMAND_MAX_WIDTH + 2) + " ~";
        String line3 = "~ :>" + " ".repeat(COMMAND_MAX_WIDTH) + " ~";
        String line4 = "~".repeat(COMMAND_MAX_WIDTH + 6);

        print(
                ANSICodes.moveCursor(startPoint),
                line1 + ANSICodes.moveCursorLeft(COMMAND_MAX_WIDTH + 6) + ANSICodes.moveCursorDown(1),
                line2 + ANSICodes.moveCursorLeft(COMMAND_MAX_WIDTH + 6) + ANSICodes.moveCursorDown(1),
                line3 + ANSICodes.moveCursorLeft(COMMAND_MAX_WIDTH + 6) + ANSICodes.moveCursorDown(1),
                line4,
                ANSICodes.moveCursor(startPoint) + ANSICodes.moveCursorRight(4) + ANSICodes.moveCursorDown(2)
        );
    }
}
