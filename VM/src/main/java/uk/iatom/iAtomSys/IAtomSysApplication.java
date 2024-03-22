package uk.iatom.iAtomSys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.iatom.iAtomSys.client.shell.ANSICodes;

@SpringBootApplication
public class IAtomSysApplication {

  public static void main(String[] args) {

    Thread hook = new Thread(() -> System.out.println(ANSICodes.getExitMetaSequence()));
    Runtime.getRuntime().addShutdownHook(hook);

    SpringApplication.run(IAtomSysApplication.class, args);
  }

}
