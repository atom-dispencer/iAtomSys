package uk.iatom.iAtomSys.server.vm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

@Component
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
  }

  private byte[] fetchNextInstructionAndArguments() {
    int programCounter = registerSet.ProgramCounter().get();
    return memory.read(programCounter, 3);
  }

  private void executeInstruction(byte[] instructionAndArguments) {
    if (instructionAndArguments.length == 0) {
      logger.warn("Cannot execute zero-byte instruction. Skipping.");
      return;
    }

    Instructions instruction = Instructions.fromByte(instructionAndArguments[0]);

    try {
      instruction.executor.exec(instructionAndArguments[1], instructionAndArguments[2], memory,
          registerSet, processorStack, flags);
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
