package uk.iatom.iAtomSys.client.shell;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@ShellComponent
@Component
public class ShellCommands {

  public static final String[] HELP_PAGES = new String[]{
      "[0] Usage: 'help <page>', or check GitHub docs.",
      "[1] 'hello': Say hi!",
      "[2] 'step <count>': Do current instruction and increment PC."
  };
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
    URI uri = UriComponentsBuilder.fromHttpUrl(formatUri("step"))
        .queryParam("count", count)
        .build().toUri();
    ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(uri, String.class);
    int statusCode = responseEntity.getStatusCodeValue();
    logger.info(String.valueOf(statusCode));
    display.draw();
  }

}
