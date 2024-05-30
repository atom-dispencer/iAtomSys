package uk.iatom.iAtomSys.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
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
import uk.iatom.iAtomSys.client.configuration.ApiClientConfiguration;
import uk.iatom.iAtomSys.client.disassembly.MemoryDisassembler;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.VMClient;
import uk.iatom.iAtomSys.common.api.VMStateRequestPacket;
import uk.iatom.iAtomSys.common.api.VMStateResponsePacket;


@ShellComponent
@Component
public class ShellCommands {

  public static final String[] HELP_PAGES = new String[]{ //
      "[0] 'help <page>': Find help! Also check GitHub docs.", //
      "[1] 'exit': Terminate the application.", //
      "[2] 'hello': Say hi!", //
      "[3] 'step <count>': Execute the next <count> instructions.", //
      "[4] 'load <image_name[.img]>': Load the given memory image.", //
      "[5] 'set <address> <value>': Set the value at the address." //
  };
  private final Logger logger = LoggerFactory.getLogger(ShellCommands.class);
  @Autowired
  private VMClient api;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ShellDisplay display;
  @Autowired
  private MemoryDisassembler memoryDisassembler;
  @Autowired
  private ApiClientConfiguration apiClientConfiguration;

  @PostConstruct
  public void postConstruct() {
    display.activate();
  }

  @PreDestroy
  public void preDestroy() {
    display.deactivate();
  }

  private void updateDisplayVMState() {

    // TODO (Solved?) Dynamic pcrOffset and sliceWidth
    VMStateResponsePacket vmStateResponsePacket = api.getState(new VMStateRequestPacket(
        apiClientConfiguration.getVmStateRequestPcrOffset(),
        apiClientConfiguration.getVmStateRequestSliceWidth()
    ));

    if (vmStateResponsePacket == null) {
      logger.error("Cannot update display VM state: received null.");
      return;
    }

    // TODO Need to handle running/not-running states as each state will display different info!
    // TODO What if state packet values are null?

    display.getDisplayState().setAvailableImages(vmStateResponsePacket.availableImages());

    display.getDisplayState()
        .setMemorySliceStartAddress(vmStateResponsePacket.memoryStartAddress());
    short[] memory = vmStateResponsePacket.memory();
    List<String[]> disassembly = memoryDisassembler.disassemble(memory);
    display.getDisplayState().setMemory(memory);
    display.getDisplayState().setDisassembly(disassembly);

    List<RegisterPacket> registers = vmStateResponsePacket.registers();
    display.getDisplayState().setRegisters(registers);

    List<Short> orderedPortAddresses = vmStateResponsePacket.orderedPortAddresses();
    display.getDisplayState().setPortAddresses(orderedPortAddresses);
  }

  @ShellMethod()
  public String exit() {
    display.getDisplayState().setCommandMessage("Shutting down application...");
    display.draw();
    ((ConfigurableApplicationContext) applicationContext).close();
    throw new ExitRequest();
  }

  @ShellMethod()
  public void help(final @ShellOption(defaultValue = "0") String pageStr) {

    try {
      int page = Integer.parseInt(pageStr);
      display.getDisplayState().setCommandMessage(HELP_PAGES[page]);

    } catch (NumberFormatException nfx) {
      display.getDisplayState()
          .setCommandMessage("Input must be an integer. Got %s.".formatted(pageStr));
    } catch (IndexOutOfBoundsException ibx) {
      display.getDisplayState().setCommandMessage(
          "%s not in range [0,%d], try 'help 0'".formatted(pageStr, HELP_PAGES.length - 1));
    }

    display.draw();
  }

  @ShellMethod()
  public void hello() {
    display.getDisplayState().setCommandMessage("Hello!");
    display.draw();
  }

  //TODO Availability methods https://docs.spring.io/spring-shell/reference/commands/availability.html
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

    updateDisplayVMState();
    display.draw();
  }

  @ShellMethod
  public void load(final @ShellOption(defaultValue = "") String imageName) {

    try {
      LoadRequestPacket request = new LoadRequestPacket(imageName);
      String message = api.loadmem(request);
      display.getDisplayState().setCommandMessage(message);

    } catch (IllegalArgumentException e) {
      help("4");
      return;
    }

    updateDisplayVMState();
    display.draw();
  }

  @ShellMethod
  public void jmp(final @ShellOption(value = "-n", defaultValue = "0") String address) {
    set("PCR*", address);
  }

  @ShellMethod
  public void set(final @ShellOption(defaultValue = "NO_ADDRESS") String address,
      final @ShellOption(defaultValue = "0") String value) {
    if (address.equals("NO_ADDRESS")) {
      help("5");
      return;
    }

    try {
      SetRequestPacket request = new SetRequestPacket(address, value);
      String message = api.set(request);
      display.getDisplayState().setCommandMessage(message);
    } catch (IllegalArgumentException e) {
      help("5");
      return;
    }

    updateDisplayVMState();
    display.draw();
  }
}
