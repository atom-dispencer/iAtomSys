package uk.iatom.iAtomSys.client.shell;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ShellBeans {

    @Bean( name = "shellDisplay")
    @Scope( value = BeanDefinition.SCOPE_SINGLETON)
    public ShellDisplay shellDisplay() {
        return new ShellDisplay();
    }
}
