package uk.iatom.iAtomSys.server.configuration;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import uk.iatom.iAtomSys.server.vm.IAtomSysVM;

@Configuration
public class ServerBeans {

  @Bean
  public VMConfiguration vmConfiguration() {
    return new VMConfiguration();
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public IAtomSysVM iAtomSysVM() {
    return new IAtomSysVM();
  }
}
