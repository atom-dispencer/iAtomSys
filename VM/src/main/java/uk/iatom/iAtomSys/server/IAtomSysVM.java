package uk.iatom.iAtomSys.server;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutor;
import uk.iatom.iAtomSys.common.instruction.InstructionSet;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutionException;
import uk.iatom.iAtomSys.server.memory.Memory;
import uk.iatom.iAtomSys.server.stack.ProcessorStack;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Getter
public class IAtomSysVM {

  private final Logger logger = LoggerFactory.getLogger(IAtomSysVM.class);


  @Autowired
  private Memory memory;

  @Autowired
  private RegisterSet registerSet;

  @Autowired
  private InstructionSet instructionSet;

  @Autowired
  private ProcessorStack processorStack;

  public void processNextCycle() {
    Register PCR = Register.PCR(registerSet);
    short pc = PCR.get();

    short instruction = memory.read(pc);
    executeInstruction(instruction);

    int newPc = pc + 1;
    if (newPc >= Short.toUnsignedInt(Short.MAX_VALUE)) {
      logger.info("PC is at max value %d".formatted(newPc));
      return;
    }

    PCR.set((short) (pc + 1));
  }

  private void executeInstruction(short int16Instruction) {

    // Check the MA flag (the least significant bit)
    // If ON, treat as a memory load command
    if (int16Instruction % 2 == 1) {
      // TODO Implement MA instruction value -> IDK loading.
    }

    // If OFF, treat as an instruction
    else {
      byte opcode = (byte) ((int16Instruction & 0xff00) >> 8);
      byte flags = (byte) (int16Instruction & 0x00ff);

      Instruction instruction = instructionSet.getInstruction(opcode);

      try {
        instruction.executor().exec(this, flags);
      } catch (InstructionExecutionException ixe) {

        if (ixe.instruction == null) {
          ixe.instruction = instruction;
        }

        String extraInformation;
        try {
          Register PCR = Register.PCR(registerSet);
          extraInformation = "PC:%d".formatted(PCR.get());
        } catch (Exception e) {
          logger.error("Error generating extra information for error.");
          extraInformation = "(Error generating extra information.)";
        }

        logger.error("Error executing instruction. %s".formatted(extraInformation), ixe);
      }
    }
  }
}
