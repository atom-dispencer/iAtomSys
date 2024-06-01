package uk.iatom.iAtomSys.server;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.instruction.InstructionSet;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.configuration.VmConfiguration;
import uk.iatom.iAtomSys.server.device.IOPort;
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

  @Autowired
  private VmConfiguration vmConfiguration;

  @Autowired
  private IOPort[] ports;

  public void processNextCycle() {
    Register PCR = Register.PCR(registerSet);
    short pc = PCR.get();

    short instruction = memory.read(pc);
    executeInstruction(instruction);

    for (IOPort port : ports) {
      port.updateFlag();
    }

    // Try to increment the program counter
    int newPc = pc + 1;
    if (newPc >= Short.toUnsignedInt(Short.MAX_VALUE)) {
      logger.info("PC is at max value %d".formatted(newPc));
      return;
    }

    PCR.set((short) (pc + 1));
  }

  private void executeInstruction(short int16Instruction) {

    // Check the MA flag (the least significant bit)
    // If treat as a memorySlice load command
    if (int16Instruction % 2 == 1) {
      Register.IDK(registerSet).set(int16Instruction);
      return;
    }

    // Try to treat it as an instruction
    byte opcode = (byte) ((int16Instruction & 0xff00) >> 8);
    byte flags = (byte) (int16Instruction & 0x00ff);
    Instruction instruction = instructionSet.getInstruction(opcode);

    // If the instruction was not understood...
    if (instruction == null) {
      logger.error("Could not decode instruction 0x%04x (skipping).".formatted(int16Instruction));
      return;
    }

    // Let's give it a go!
    try {
      instruction.executor().exec(this, flags);
    } catch (InstructionExecutionException ixe) {

      // So that the instruction doesn't need to be passed
      //   to the executor.
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
