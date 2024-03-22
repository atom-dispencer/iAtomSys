package uk.iatom.iAtomSys.client.shell;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.ExitRequest;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;

public class ShellDisplay {

    private final Logger logger = LoggerFactory.getLogger(ShellDisplay.class);

    @Getter
    private final ShellDisplayState state = new ShellDisplayState();

    private boolean alive;

    private static final Size DEFAULT_SIZE = new Size(120, 30);
    private Terminal terminal;
    private final Supplier<Point> COMMAND_BOX_POS = () -> new Point(5, terminal.getSize().getRows() - 10);
    private final int COMMAND_MAX_WIDTH = 64;
    private final PrintStream sysOutCache = System.out;

    public void activate() {
        if (alive) return;
        this.alive = true;

        try {
            terminal = TerminalBuilder.terminal();
        } catch (IOException iox) {
            logger.error("Error activating ShellDisplay. Could not create new Terminal. Cannot continue.", iox);
            System.exit(-1);
        }

        logger.info("Activating ShellDisplay...");
        // TODO If zero, error and exit
        logger.info("Terminal size is: %s".formatted(terminal.getSize()));


        print(
                ANSICodes.NEW_BUFFER,
                ANSICodes.YOU_ARE_DRUNK
        );

        // disableSysOut();

        state.setCommandMessage("Enter a command below to get started!");
        draw();

        System.out.println("If you can see this, System.out has not been disabled.");
    }

    public void deactivate() {
        if (!alive) return;
        this.alive = false;
        logger.info("Deactivating ShellDisplay...");


        try {
            terminal.close();
        } catch (IOException iox) {
            logger.error("Error closing old terminal.", iox);
        } finally {
            print(ANSICodes.OLD_BUFFER);
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

    public void enableSysOut() {
        System.setOut(sysOutCache);
    }

    public void disableSysOut() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // Do nothing, stream is dead.
            }
        }));
    }


    private void assertShellLive() {
        assert alive : new IllegalStateException("ShellDisplay cannot take actions while the shell is not live.");
    }

    /**
     * Immediately redraw the UI with the current {@link ShellDisplayState}.
     */
    public void draw() {
        assertShellLive();

        drawBackground();
        drawCommandInput(state.getCommandMessage());
    }

    private void drawBackground() {
        assertShellLive();
        int preHeadingWidth = 10;
        String preHeading = "#".repeat(preHeadingWidth);
        String heading = " iAtomSysVM ";
        int postHeadingWidth = terminal.getSize().getColumns() - 2 - preHeadingWidth - heading.length();
        String postHeading = "#".repeat(postHeadingWidth);

        String headerLine = " %s%s%s ".formatted(preHeading, heading, postHeading);
        String midLine = " #" + ANSICodes.moveCursorHorizontalAbsolute(terminal.getSize().getColumns() - 1) + "# ";
        String footerLine = " " + "#".repeat(terminal.getSize().getColumns() - 2) + " ";

        print(
                ANSICodes.PUSH_CURSOR_POS,
                ANSICodes.YOU_ARE_DRUNK,
                ANSICodes.CLEAR_SCREEN,
                headerLine + "\n",
                (midLine + "\n").repeat(terminal.getSize().getRows() - 2),
                footerLine,
                ANSICodes.POP_CURSOR_POS
        );
    }

    /**
     * Reset the typing-cursor to the start of the command input box and clear the current command input.
     */
    private void drawCommandInput(String commandMessage) {
        //TODO should drawCommandInput clear the current command? Maybe need to flush System.in?
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
