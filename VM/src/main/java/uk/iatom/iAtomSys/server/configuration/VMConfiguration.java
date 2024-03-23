package uk.iatom.iAtomSys.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("iatomsys.vm")
public class VMConfiguration {

  public int millisPerCycle = 1000;

  public int integerWidthBytes;

  public int processorStackSizeInts;
}
