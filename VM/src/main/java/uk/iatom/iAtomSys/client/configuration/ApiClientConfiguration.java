package uk.iatom.iAtomSys.client.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("iatomsys.client")
public class ApiClientConfiguration {

  private short vmStateRequestPcrOffset = -16;
  private short vmStateRequestSliceWidth = 128;
}
