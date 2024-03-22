package uk.iatom.iAtomSys.client.shell.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.shell.ShellApplicationRunner;
import org.springframework.shell.ShellRunner;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeSettings;
import uk.iatom.iAtomSys.client.shell.ShellDisplay;
import uk.iatom.iAtomSys.client.shell.WrappedInteractiveShellApplicationRunner;

import java.util.List;

@Configuration
public class ShellBeans {

    private final Logger logger = LoggerFactory.getLogger(ShellBeans.class);

    @Bean(name = "shellDisplay")
    @Scope(value = BeanDefinition.SCOPE_SINGLETON)
    public ShellDisplay shellDisplay() {
        return new ShellDisplay();
    }


    @Bean(name = "shellTheme")
    public Theme shellTheme() {
        return new Theme() {
            @Override
            public String getName() {
                return "iAtomSysShellTheme";
            }

            @Override
            public ThemeSettings getSettings() {
                return new ShellThemeSettings();
            }
        };
    }

    @Bean
    public CommandNotFoundConfiguration commandNotFoundConfiguration() {
        return new CommandNotFoundConfiguration();
    }

    @Bean
    public CustomExceptionResolver customExceptionResolver() {
        return new CustomExceptionResolver();
    }

    // TODO Consider removal

    /**
     * Create a {@link ShellApplicationRunner} controlled by me in an attempt to get rid of rogue error messages polluting the UI, including when
     * Spring Shell tries to parse 'help -1' and thinks '-1' is a switch.
     *
     * @param shellRunners
     * @return
     */
    //@Bean
    public ShellApplicationRunner shellApplicationRunner(List<ShellRunner> shellRunners) {
        logger.warn("Interrupted Spring Shell ShellApplicationRunner Bean! Reconfiguring...");
        return new WrappedInteractiveShellApplicationRunner(shellRunners);
    }
}
