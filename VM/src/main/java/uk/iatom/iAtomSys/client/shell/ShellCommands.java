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

    @PostConstruct
    public void postConstruct() {
        shellDisplay.activate();
    }

    @PreDestroy
    public void preDestroy() { shellDisplay.deactivate(); }

    @Value("${server.port}")
    int port;

    @ShellMethod(key = "hello")
    public String hello() {
        return "World!";
    }

    @ShellMethod(key = "shutdown")
    public String shutdown() {
        shellDisplay.showShortMessage("Shutting down application...");
        System.out.println("lol Shutting down application...");
        ((ConfigurableApplicationContext) applicationContext).close();
        throw new ExitRequest();
    }

    @ShellMethod(key = "testing")
    public void testing() {
        shellDisplay.showShortMessage("Stuff and things!");
    }

    @ShellMethod(key = "step")
    public void step(
            final @ShellOption(value = "-n", defaultValue = "1") int count
    ) {
        URI uri = UriComponentsBuilder.fromHttpUrl(formatUri("step"))
                .queryParam("count", count)
                .build().toUri();
        ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(uri, String.class);
        int statusCode = responseEntity.getStatusCodeValue();
        logger.info(String.valueOf(statusCode));
    }

    private String formatUri(final String endpoint) {
        return "http://localhost:%d/%s".formatted(port, endpoint);
    }
}
