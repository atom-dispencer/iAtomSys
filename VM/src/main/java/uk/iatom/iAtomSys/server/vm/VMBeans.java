package uk.iatom.iAtomSys.server.vm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.iatom.iAtomSys.server.configuration.VMConfiguration;
import uk.iatom.iAtomSys.server.vm.memory.ByteArrayMemory;
import uk.iatom.iAtomSys.server.vm.memory.DataSizes;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.InMemoryRegister;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.InMemoryProcessorStack;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

@Configuration
public class VMBeans {

  @Autowired
  private VMConfiguration vmConfiguration;

  @Bean
  public DataSizes dataSizes() {
    return new DataSizes(0, 0);
  }

  @Bean
  public Memory memory(DataSizes dataSizes) {
    return new ByteArrayMemory(new byte[(int) Math.pow(2, 16)]);
  }

  @Bean
  public RegisterSet registerSet(DataSizes dataSizes, Memory memory) {
    return new RegisterSet(
        new InMemoryRegister(0, dataSizes, memory),
        new InMemoryRegister(RegisterSet.ACCUMULATOR_INDEX * dataSizes.integerBytes(), dataSizes,
            memory),
        new InMemoryRegister(RegisterSet.RETURN_INDEX * dataSizes.integerBytes(), dataSizes,
            memory),
        new InMemoryRegister(RegisterSet.POP_INDEX * dataSizes.integerBytes(), dataSizes, memory),
        new InMemoryRegister(RegisterSet.NUMERIC_INDEX * dataSizes.integerBytes(), dataSizes,
            memory),
        new InMemoryRegister(RegisterSet.A_INDEX * dataSizes.integerBytes(), dataSizes, memory)
    );
  }

  @Bean
  public ProcessorStack processorStack(DataSizes dataSizes, RegisterSet registerSet,
      Memory memory) {
    return new InMemoryProcessorStack(
        registerSet.Pop(),
        memory,
        (RegisterSet.A_INDEX + 1) * dataSizes.integerBytes(),
        vmConfiguration.processorStackSizeInts * dataSizes.integerBytes()
    );
  }
}
