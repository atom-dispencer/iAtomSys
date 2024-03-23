package uk.iatom.iAtomSys.server.vm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.iatom.iAtomSys.server.configuration.VMConfiguration;
import uk.iatom.iAtomSys.server.vm.memory.ByteArrayMemory;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.InMemoryRegister;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.InMemoryProcessorStack;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

@Configuration
public class VMBeans {

  @Autowired
  private VMConfiguration vmConfiguration;

  @Bean(BeanDefinition.SCOPE_SINGLETON)
  public Memory memory() {
    return new ByteArrayMemory(new byte[(int) Math.pow(2, 16)]);
  }

  @Bean(BeanDefinition.SCOPE_SINGLETON)
  public RegisterSet registerSet(Memory memory) {
    return new RegisterSet(
        new InMemoryRegister(0, memory),
        new InMemoryRegister(RegisterSet.INDEX_ACCUMULATOR * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_RETURN * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_IO_STACK * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_IO_MEMORY * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_NUMERIC * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_A * 2, memory));
  }

  @Bean(BeanDefinition.SCOPE_SINGLETON)
  public ProcessorStack processorStack(RegisterSet registerSet,
      Memory memory) {
    return new InMemoryProcessorStack(
        registerSet.IOStack(),
        memory,
        (RegisterSet.INDEX_A + 1) * 2,
        vmConfiguration.processorStackSizeInts * 2
    );
  }

  @Bean(BeanDefinition.SCOPE_SINGLETON)
  public Flags flags() {
    return new Flags();
  }
}
