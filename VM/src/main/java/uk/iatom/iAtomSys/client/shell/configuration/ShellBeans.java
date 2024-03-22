package uk.iatom.iAtomSys.client.shell.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeSettings;
import uk.iatom.iAtomSys.client.shell.ShellDisplay;

@Configuration
public class ShellBeans {

    @Bean( name = "shellDisplay")
    @Scope( value = BeanDefinition.SCOPE_SINGLETON)
    public ShellDisplay shellDisplay() {
        return new ShellDisplay();
    }


    @Bean( name = "shellTheme")
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
}
