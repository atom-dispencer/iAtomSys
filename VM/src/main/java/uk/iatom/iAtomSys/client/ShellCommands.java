package uk.iatom.iAtomSys.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;
import uk.iatom.iAtomSys.common.api.RunRequestPacket;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.VmClient;
import uk.iatom.iAtomSys.common.api.VmStatus;


@ShellComponent
@Component
@Getter
public class ShellCommands {

  private static final Logger logger = LoggerFactory.getLogger(ShellCommands.class);

  public static final String[] HELP_PAGES = new String[]{ //
      "[0] 'help <page 0-9>': Find help! Also check GitHub docs.", //
      "[1] 'exit': Terminate the application.", //
      "[2] 'hello': Say hi!", //
      "[3] 'step <count>': Execute the next <count> instructions.", //
      "[4] 'load <image_name[.img]>': Load the given memorySlice image.", //
      "[5] 'jmp <address>': (Shorthand) PCR* <address>.", //
      "[6] 'set <address> <value>': Set the value at the address.", //
      "[7] 'dropDebug': Reset the loaded debug symbols.", //
      "[8] 'run <x|start> <end?>': Execute between the addresses.", //
      "[9] 'refresh': Refreshes the display state and redraw." //
  };

  // Messages which may appear in the ShellDisplayState command message
  public static final Function<String, String> HELP_BAD_FORMAT = "Input must be an integer. Got %s."::formatted;
  public static final Function<String, String> HELP_BAD_INDEX = (pageStr) -> "%s not in range [0,%d], try 'help 0'".formatted(pageStr, HELP_PAGES.length - 1);
  public static final String EXIT_SHUTDOWN = "Shutting down application...";

  @Autowired
  private ApplicationContext applicationContext;
  private final VmClient api;
  private final ShellDisplay display;
  private final AtomicBoolean shouldResetCommand = new AtomicBoolean(false);

  @Autowired
  public ShellCommands(VmClient api, ShellDisplay display) {
    this.api = api;
    this.display = display;
  }

  /**
   * The task responsible for repeatedly refreshing the UI while the VM is in the
   * {@link VmStatus#RUNNING} phase.
   */
  private final Runnable updateDaemonTask = new Thread(() -> {
    logger.info("Starting update daemon...");

    while (getDisplay().getDisplayState().getStatus() == VmStatus.RUNNING && getDisplay().isAlive()) {
      try {
        getDisplay().getDisplayState().update();
        getDisplay().draw(shouldResetCommand.getAndSet(false));
        Thread.sleep(1000L);
      } catch (Exception e) {
        logger.error("Suppressed error in run/sleep loop.", e);
      }
    }

    logger.info("Update daemon exiting...");
  });
  private Thread updateDaemon = null;

  @PostConstruct
  public void postConstruct() {
    display.activate();
  }

  @PreDestroy
  public void preDestroy() {
    display.deactivate();
  }

  @ShellMethod()
  public String exit() {
    display.getDisplayState().setCommandMessage(EXIT_SHUTDOWN);
    display.draw(true);
    ((ConfigurableApplicationContext) applicationContext).close();
    throw new ExitRequest();
  }

  /**
   * Try to update the local state from the remote and redraw the UI, refraining if the
   * {@link #updateDaemonTask} is running because that could cause race conditions.
   *
   * @param onlyRedraw Whether to only redraw the UI and *not* update the local state.
   */
  private void tryRefresh(boolean onlyRedraw) {
    if (updateDaemon == null || !updateDaemon.isAlive()) {
      if (!onlyRedraw) {
        display.getDisplayState().update();
      }

      display.draw(true);
    } else {
      shouldResetCommand.set(true);
    }
  }

  @ShellMethod()
  public void help(final @ShellOption(defaultValue = "0") String pageStr) {

    try {
      int page = Integer.parseInt(pageStr);
      display.getDisplayState().setCommandMessage(HELP_PAGES[page]);

    } catch (NumberFormatException nfx) {
      display.getDisplayState().setCommandMessage(HELP_BAD_FORMAT.apply(pageStr));
    } catch (IndexOutOfBoundsException ibx) {
      display.getDisplayState().setCommandMessage(HELP_BAD_INDEX.apply(pageStr));
    }

    tryRefresh(true);
  }

  @ShellMethod()
  public void hello() {
    display.getDisplayState().setCommandMessage("Hello!");

    tryRefresh(true);
  }

  @ShellMethod()
  public void step(final @ShellOption(value = "-n", defaultValue = "1") int count) {

    try {
      StepRequestPacket packet = new StepRequestPacket(count);
      String message = api.step(packet);
      display.getDisplayState().setCommandMessage(message);
    } catch (IllegalArgumentException iax) {
      help("3");
      return;
    }

    tryRefresh(false);
  }

  @ShellMethod
  public void load(final @ShellOption(defaultValue = "") String imageName) {

    try {
      LoadRequestPacket request = new LoadRequestPacket(imageName);
      String message = api.load(request);
      display.getDisplayState().setCommandMessage(message);

    } catch (IllegalArgumentException e) {
      help("4");
      return;
    }

    tryRefresh(false);
  }

  @ShellMethod
  public void jmp(final @ShellOption(value = "-n", defaultValue = "0") String address) {
    set("PCR", address);
  }

  @ShellMethod
  public void set(final @ShellOption(defaultValue = "NO_ADDRESS") String address,
      final @ShellOption(defaultValue = "0") String value) {
    if (address.equals("NO_ADDRESS")) {
      help("6");
      return;
    }

    try {
      SetRequestPacket request = new SetRequestPacket(address, value);
      String message = api.set(request);
      display.getDisplayState().setCommandMessage(message);
    } catch (IllegalArgumentException e) {
      help("6");
      return;
    }

    tryRefresh(false);
  }

  @ShellMethod
  public void drop_debug() {
    try {
      String message = api.drop_debug();
      display.getDisplayState().setCommandMessage(message);
    } catch (IllegalArgumentException e) {
      help("7");
    }

    tryRefresh(false);
  }

  @ShellMethod
  public void run(final @ShellOption(defaultValue = "here") String startAddressStr) {

    try {
      // Redraw to clean the command input
      tryRefresh(true);

      RunRequestPacket packet = new RunRequestPacket(startAddressStr);
      String message = api.run(packet);
      display.getDisplayState().setCommandMessage(message);

      // Refresh to get the new state (is the VM running?)
      tryRefresh(false);

      // If the VM is running, start the update daemon if it isn't already going
      if ((updateDaemon == null || !updateDaemon.isAlive())
          && display.getDisplayState().getStatus() == VmStatus.RUNNING) {
        updateDaemon = new Thread(updateDaemonTask);
        updateDaemon.setDaemon(true);
        updateDaemon.start();
      }
    } catch (IllegalArgumentException e) {
      logger.error("Run command", e);
      help("8");
    }
  }

  @ShellMethod
  public void refresh() {
    tryRefresh(false);
  }

  @ShellMethod
  public void pause() {
    try {
      String message = api.pause();
      display.getDisplayState().setCommandMessage(message);
    } catch (IllegalArgumentException e) {
      help("10");
    }

    tryRefresh(false);
  }
}
