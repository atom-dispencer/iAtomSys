package uk.iatom.iAtomSys.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.iatom.iAtomSys.api.VMStatePacket;
import uk.iatom.iAtomSys.client.decode.DecodedInstruction;
import uk.iatom.iAtomSys.client.decode.DecodedRegister;
import uk.iatom.iAtomSys.client.decode.InstructionReader;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;

@ShellComponent
@Component
public class ShellCommands {

  public static final String[] HELP_PAGES = new String[]{
      "[0] Usage: 'help <page>', or check GitHub docs.", "[1] 'hello': Say hi!",
      "[2] 'step <count>': Do current instruction and increment PC."};
  private final Logger logger = LoggerFactory.getLogger(ShellCommands.class);
  @Value("${server.port}")
  int port;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ShellDisplay display;

  @PostConstruct
  public void postConstruct() {
    display.activate();
  }

  @PreDestroy
  public void preDestroy() {
    display.deactivate();
  }

  private String formatUri(final String endpoint) {
    return "http://localhost:%d/%s".formatted(port, endpoint);
  }

  private RequestResult send(String endpoint, String key, String value) {
    String uriBase = formatUri(endpoint);
    URI uri = UriComponentsBuilder.fromHttpUrl(uriBase).queryParam(key, value).build().toUri();
    ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(uri, String.class);

    HttpStatusCode status = responseEntity.getStatusCode();
    if (status.isError()) {
      logger.error("Request to %s returned a HTTP error: %s".formatted(uriBase, status));
    } else {
      logger.info("Request to %s returned a HTTP success: %s".formatted(uriBase, status));
    }

    return new RequestResult(status, responseEntity.getBody());
  }

  public record RequestResult(HttpStatusCode status, String message) { }

  private void updateDisplayVMState() {
    String uriBase = formatUri("state");
    URI uri = UriComponentsBuilder.fromHttpUrl(uriBase).queryParam("memoryByteCount", 16).build().toUri();
    ResponseEntity<VMStatePacket> response = new RestTemplate().getForEntity(uri, VMStatePacket.class);

    HttpStatusCode status = response.getStatusCode();
    if (status.isError()) {
      logger.error("Request to %s returned a HTTP error: %s".formatted(uri, status));
    } else {
      logger.info("Request to %s returned a HTTP success: %s".formatted(uri, status));
    }

    if (response.getBody() == null) {
      logger.error("Somehow got a null response body from updating VM state?");
      display.getState().setCommandMessage("Bad VMState response - please report to developer.");
      return;
    }

    InstructionReader reader = new InstructionReader(response.getBody().memory());
    List<DecodedInstruction> instructions = new ArrayList<>();
    while(reader.hasNextByte()) {
      instructions.add(reader.readNextInstruction());
    }

    DecodedRegister flags = new DecodedRegister(RegisterSet.NAMES.getKey(RegisterSet.INDEX_FLAGS), "????");
    List<DecodedRegister> registers = new ArrayList<>();
    for(Entry<String, Integer> entry : response.getBody().registers().entrySet()) {
      String name = entry.getKey();
      int value = entry.getValue();
      DecodedRegister register = new DecodedRegister(name, String.format("%04x", value));

      if (RegisterSet.NAMES.getKey(RegisterSet.INDEX_FLAGS).equals(name)) {
        flags = register;
      } else {
        registers.add(register);
      }
    }

    display.getState().setMemory(instructions);
    display.getState().setRegisters(registers);
    display.getState().setFlags(flags);
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

    if (count < 1 || count > 256) {
      display.getState()
          .setCommandMessage("Count must be in interval [1,256], got %d".formatted(count));
    } else {
      try {
        RequestResult result = send("step", "count", Integer.toString(count));
        if (result.status.isError()) {
          display.getState().setCommandMessage("Failure: %s".formatted(result.message));
        } else {
          display.getState().setCommandMessage("Stepped %d cycles".formatted(count));
        }
      } catch (Exception e) {
        logger.error("Error executing send request.", e);
        display.getState()
            .setCommandMessage("Request error: %s".formatted(e.getClass().getSimpleName()));
      }
    }

    updateDisplayVMState();
    display.draw();
  }

  @ShellMethod
  public void loadmem(final @ShellOption(defaultValue = "") String imageName) {
    if (imageName.isBlank()) {
      display.getState().setCommandMessage("Usage: 'loadmem <file name>'.");
    } else {
      String name = new File(imageName).getName();
      String urlEncoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
      display.getState().setCommandMessage("Loading: %s".formatted(name));

      try {
        RequestResult result = send("loadmem", "file", urlEncoded);
        if (result.status.isError()) {
          display.getState().setCommandMessage("Failure: %s".formatted(result.message));
        } else {
          display.getState().setCommandMessage("Loaded %s".formatted(name));
        }
      } catch (Exception e) {
        logger.error("Error executing loadmem request.", e);
        display.getState()
            .setCommandMessage("Request error: %s".formatted(e.getClass().getSimpleName()));
      }
    }

    updateDisplayVMState();
    display.draw();
  }


}
