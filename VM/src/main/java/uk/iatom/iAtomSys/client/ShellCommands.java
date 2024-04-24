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
import uk.iatom.iAtomSys.client.disassembly.MemoryDisassembler;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;
import uk.iatom.iAtomSys.common.api.LoadImageRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.VMClient;
import uk.iatom.iAtomSys.common.api.VMStateRequestPacket;
import uk.iatom.iAtomSys.common.api.VMStateResponsePacket;
import uk.iatom.iAtomSys.common.register.RegisterSet;


@ShellComponent
@Component
public class ShellCommands {

  public static final String[] HELP_PAGES = new String[]{
      "[0] Usage: 'help <page>', or check GitHub docs.", "[1] 'hello': Say hi!",
      "[2] 'step <count>': Execute the next <count> instructions."};
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
  private RegisterSet registerSet;

  @PostConstruct
  public void postConstruct() {
    display.activate();
  }

  @PreDestroy
  public void preDestroy() {
    display.deactivate();
  }

  private void updateDisplayVMState() {

    VMStateResponsePacket vm = api.getState(new VMStateRequestPacket((short) -8, (short) 17));

    if (vm == null) {
      logger.error("Cannot update display VM state: received null.");
      return;
    }

    // TODO Need to handle running/not-running states as each state will display different info!
    // TODO What if state packet values are null?

    short[] shorts = vm.memory();
    List<String[]> instructions = memoryDisassembler.disassemble(shorts);
    display.getState().setInstructions(instructions);

    List<RegisterPacket> registers = vm.registers();
    display.getState().setRegisters(registers);
  }

  @ShellMethod()
  public void hello() {
    display.getState().setCommandMessage("Hello!");
    display.draw();
  }

  @ShellMethod()
  public String exit() {
    display.getState().setCommandMessage("Shutting down application...");
    display.draw();
    ((ConfigurableApplicationContext) applicationContext).close();
    throw new ExitRequest();
  }

  @ShellMethod()
  public void help(final @ShellOption(defaultValue = "0") int page) {
    if (0 <= page && page < HELP_PAGES.length) {
      display.getState().setCommandMessage(HELP_PAGES[page]);
    } else {
      display.getState().setCommandMessage(
          "%d not in range [0,%d], try 'help 0'".formatted(page, HELP_PAGES.length - 1));
    }
    display.draw();
  }

  //TODO Availability methods https://docs.spring.io/spring-shell/reference/commands/availability.html
  @ShellMethod()
  public void step(final @ShellOption(value = "-n", defaultValue = "1") int count) {
    String message = api.step(new StepRequestPacket(count));
    display.getState().setCommandMessage(message);

    updateDisplayVMState();
    display.draw();
  }

  @ShellMethod
  public void loadImage(final @ShellOption(defaultValue = "") String imageName) {

    if (imageName.isBlank()) {
      display.getState().setCommandMessage("Usage: 'loadImage <file name>'.");
    }

    String message = api.loadmem(new LoadImageRequestPacket(imageName));
    display.getState().setCommandMessage(message);

    updateDisplayVMState();
    display.draw();
  }
}
