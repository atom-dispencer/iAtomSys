package uk.iatom.iAtomSys.client.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeSettings;
import uk.iatom.iAtomSys.client.ShellDisplay;
import uk.iatom.iAtomSys.client.disassembly.InstructionDisassembler;

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
}
