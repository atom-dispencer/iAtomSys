package uk.iatom.iAtomSys.server.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import uk.iatom.iAtomSys.server.vm.Flags;
import uk.iatom.iAtomSys.server.vm.memory.ByteArrayMemory;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.InMemoryRegister;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.Int16InMemoryProcessorStack;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

@Configuration
public class VMBeans {

  private final Logger logger = LoggerFactory.getLogger(VMBeans.class);

  @Bean
  public VMConfiguration vmConfiguration() {
    return new VMConfiguration();
  }

//  @Bean
//  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
//  public IAtomSysVM iAtomSysVM() {
//    logger.info("Creating VM bean");
//    return new IAtomSysVM();
//  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public Memory memory() {
    return new ByteArrayMemory(new byte[(int) Math.pow(2, 16)]);
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public RegisterSet registerSet(Memory memory) {
    logger.info("Creating register set");
    return new RegisterSet(
        new InMemoryRegister(0, memory),
        new InMemoryRegister(RegisterSet.INDEX_ACCUMULATOR * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_RETURN * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_IO_STACK * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_IO_MEMORY * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_POINTER * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_NUMERIC * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_IDK * 2, memory),
        new InMemoryRegister(RegisterSet.INDEX_FLAGS * 2, memory));
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public ProcessorStack processorStack(VMConfiguration vmConfiguration, RegisterSet registerSet,
      Memory memory) {
    return new Int16InMemoryProcessorStack(
        registerSet.IOStack(),
        memory,
        (RegisterSet.INDEX_IDK + 1) * 2,
        vmConfiguration.processorStackSizeIntegers * 2
    );
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public Flags flags() {
    return new Flags();
  }
}
