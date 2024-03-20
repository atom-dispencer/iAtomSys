package uk.iatom.iAtomSys.server.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("")
public class RestController {

    Logger logger = LoggerFactory.getLogger(RestController.class);

    @GetMapping("/hello")
    public String hello() {
        return "World";
    }

    @GetMapping("/step")
    public void step(@RequestParam int count) {
        logger.info("Step: %d".formatted(count));
    }

}
