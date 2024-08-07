package uk.iatom.iAtomSys.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Inputs to the VM, not directly managed by the VM, which can be set both before and during
 * runtime.
 */
@Data
@ConfigurationProperties("iatomsys.vm")
public class VmConfiguration {

  private int processorStackSizeIntegers = 256;

  private char portsRangeStartAddress = 0x000a;
}
