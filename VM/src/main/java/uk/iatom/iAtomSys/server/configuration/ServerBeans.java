package uk.iatom.iAtomSys.server.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import uk.iatom.iAtomSys.common.instruction.FlagHelper.Flag;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.device.IOPort;
import uk.iatom.iAtomSys.server.memory.Memory;
import uk.iatom.iAtomSys.server.stack.ProcessorStack;

@Configuration
public class ServerBeans {

  @Bean
  public VmConfiguration vmConfiguration() {
    return new VmConfiguration();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public Memory memory() {
    return new Memory();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public ProcessorStack processorStack(VmConfiguration vmConfiguration, RegisterSet registerSet,
      Memory memory) {
    return new ProcessorStack(
        memory,
        (char) 1000,
        vmConfiguration.getProcessorStackSizeIntegers() * 2
    );
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public IOPort[] ports(VmConfiguration vmConfiguration, RegisterSet registerSet, Memory memory) {
    return new IOPort[]{
        new IOPort(vmConfiguration.getPortsRangeStartAddress(), Flag.IO0, registerSet, memory),
        new IOPort((char) (vmConfiguration.getPortsRangeStartAddress() + 1), Flag.IO1, registerSet,
            memory),
        new IOPort((char) (vmConfiguration.getPortsRangeStartAddress() + 2), Flag.IO2, registerSet,
            memory),
        new IOPort((char) (vmConfiguration.getPortsRangeStartAddress() + 3), Flag.IO3, registerSet,
            memory),
    };
  }
}
