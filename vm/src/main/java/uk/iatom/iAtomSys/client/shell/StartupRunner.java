package uk.iatom.iAtomSys.client.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.shell.Shell;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    ShellDisplay shellDisplay;

    @Override
    public void run(String... args) throws Exception {
        // Your startup logic here
        System.out.println("Welcome to My Spring Shell Application!");
        System.out.println("Initializing...");
        // Any other initialization logic you want to include
    }
}