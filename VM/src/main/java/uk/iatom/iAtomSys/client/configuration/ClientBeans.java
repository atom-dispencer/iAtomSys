package uk.iatom.iAtomSys.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.iatom.iAtomSys.client.RemoteVMClient;

@Configuration
public class ClientBeans {

  @Bean
  public RemoteVMClient remoteVMClient() {
    return new RemoteVMClient("http://localhost:8080/");
  }

  @Bean
  public ApiClientConfiguration apiClientConfiguration() {
    return new ApiClientConfiguration();
  }

}
