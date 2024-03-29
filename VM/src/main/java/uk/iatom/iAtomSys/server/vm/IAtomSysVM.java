package uk.iatom.iAtomSys.server.vm;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.server.vm.exception.InstructionExecutionException;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Getter
public class IAtomSysVM {

  private Logger logger = LoggerFactory.getLogger(IAtomSysVM.class);


  @Autowired
  private Memory memory;

  @Autowired
  private RegisterSet registerSet;

  @Autowired
  private ProcessorStack processorStack;

  @Autowired
  private Flags flags;


  public void processNextCycle() {
    byte[] instructionAndArguments = fetchNextInstructionAndArguments();
    executeInstruction(instructionAndArguments);

    int pc = registerSet.ProgramCounter().get();
    registerSet.ProgramCounter().set(pc + 1);
  }

  private byte[] fetchNextInstructionAndArguments() {
    int programCounter = registerSet.ProgramCounter().get();
    return memory.read(programCounter, 3);
  }

  private void executeInstruction(byte[] instructionAndArguments) {
    if (instructionAndArguments.length == 0) {
      // TODO Stop VM at end of memory.
      logger.warn("Cannot execute zero-byte instruction. Skipping.");
      return;
    }

    Instructions instruction = Instructions.fromByte(instructionAndArguments[0]);

    byte msByte = 0;
    byte lsByte = 0;
    if (instructionAndArguments.length >= 2) {
      msByte = instructionAndArguments[1];
    }
    if (instructionAndArguments.length >= 3) {
      msByte = instructionAndArguments[2];
    }

    try {
      instruction.executor.exec(msByte, lsByte, memory, registerSet, processorStack, flags);
    } catch (InstructionExecutionException ixe) {

      String extraInformation;
      try {
        extraInformation = "PC:%d".formatted(registerSet.ProgramCounter().get());
      } catch (Exception e) {
        logger.error("Error generating extra information for error.");
        extraInformation = "(Error generating extra information.)";
      }

      logger.error("Error executing instruction. %s".formatted(extraInformation), ixe);
    }
  }
}
