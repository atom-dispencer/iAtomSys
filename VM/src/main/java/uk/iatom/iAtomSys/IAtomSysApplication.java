package uk.iatom.iAtomSys;

import java.util.Properties;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
public class IAtomSysApplication {

  private static final Logger logger = LoggerFactory.getLogger(IAtomSysApplication.class);

  @Getter
  private static String cicdVersion;

  public static void main(String[] args) {

    // Load properties from the build pipeline
    try {
      ClassPathResource resource = new ClassPathResource("ci-cd.properties");

      Properties properties = new Properties();
      properties.load(resource.getInputStream());

      cicdVersion = properties.getProperty("CICD_VERSION");
      System.out.println("CICD_VERSION=" + cicdVersion);

    } catch (Exception e) {
      System.err.println("Failed to load properties: " + e.getMessage());
      e.printStackTrace();
      return;
    }

    // BOING!
    // (Geddit...? Coz it's a "Spring"...?)
    ConfigurableApplicationContext context = SpringApplication.run(IAtomSysApplication.class, args);
  }

}
