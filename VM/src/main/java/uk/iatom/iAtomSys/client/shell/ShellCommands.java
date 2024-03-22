package uk.iatom.iAtomSys.client.shell;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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

import java.net.URI;

@ShellComponent
@Component
public class ShellCommands {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ShellDisplay shellDisplay;

    private final Logger logger = LoggerFactory.getLogger(ShellCommands.class);

    public static final String[] HELP_PAGES = new String[]{
            "[0] Usage: 'help <page>', or check GitHub docs.",
            "[1] 'hello': Say hi!",
            "[2] 'step <count>': Do current instruction and increment PC."
    };

    @PostConstruct
    public void postConstruct() {
        shellDisplay.activate();
    }

    @PreDestroy
    public void preDestroy() {shellDisplay.deactivate();}

    @Value("${server.port}")
    int port;

    private String formatUri(final String endpoint) {
        return "http://localhost:%d/%s".formatted(port, endpoint);
    }

    @ShellMethod()
    public void hello() {
        shellDisplay.onAnyCommand();
        shellDisplay.drawShortMessage("Hello!");
    }

    @ShellMethod()
    public String exit() {
        shellDisplay.onAnyCommand();
        shellDisplay.drawShortMessage("Shutting down application...");
        ((ConfigurableApplicationContext) applicationContext).close();
        throw new ExitRequest();
    }

    @ShellMethod()
    public void help(final @ShellOption(defaultValue = "0") int page) {
        shellDisplay.onAnyCommand();

        System.out.println("YOOOOOO");
        System.out.println("YOOOOOO");
        System.out.println("YOOOOOO");
        System.out.println("YOOOOOO");

        if (0 <= page && page < HELP_PAGES.length) {
            shellDisplay.drawShortMessage(HELP_PAGES[page]);
        } else {
            shellDisplay.drawShortMessage("%d not in range [0,%d], try 'help 0'".formatted(page, HELP_PAGES.length - 1));
        }
    }

    //TODO Availability methods https://docs.spring.io/spring-shell/reference/commands/availability.html
    @ShellMethod()
    public void step(final @ShellOption(value = "-n", defaultValue = "1") int count) {
        shellDisplay.onAnyCommand();
        URI uri = UriComponentsBuilder.fromHttpUrl(formatUri("step"))
                .queryParam("count", count)
                .build().toUri();
        ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(uri, String.class);
        int statusCode = responseEntity.getStatusCodeValue();
        logger.info(String.valueOf(statusCode));
    }

}
