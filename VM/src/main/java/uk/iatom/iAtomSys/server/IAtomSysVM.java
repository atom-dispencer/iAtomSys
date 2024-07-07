package uk.iatom.iAtomSys.server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.VmStatus;
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
  private final AsyncRunData asyncRunData = new AsyncRunData();
  @Setter
  private VmStatus status = VmStatus.STOPPED;
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
  @Setter
  private DebugSymbols debugSymbols = DebugSymbols.empty();
  private List<Character> breakpoints = new ArrayList<>();

  public void runAsync() {
    Thread thread = new Thread(() -> {

      try {
        asyncRunData.getAsyncExecutedInstructions().set(0L);
        asyncRunData.setStartTime(LocalDateTime.now());

        while (status == VmStatus.RUNNING) {
          char pcr = processNextCycle();
          asyncRunData.getAsyncExecutedInstructions().getAndIncrement();

          if (breakpoints.contains(pcr)) {
            status = VmStatus.PAUSED;
          }
        }
      } catch (Exception e) {
        logger.error("Error in async running.", e);
      }
    });

    status = VmStatus.RUNNING;
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Perform a single fetch-execute cycle, incrementing the program counter.
   */
  public char processNextCycle() {
    Register PCR = Register.PCR(registerSet);
    char pc = PCR.get();

    char instruction = memory.read(pc);
    executeInstruction(instruction);

    for (IOPort port : ports) {
      port.updateFlag();
    }

    // Only increment PCR if it hasn't been changed during the cycle, i.e. an instruction trying
    // to jump/branch.
    if (pc == PCR.get()) {
      if (pc == Character.MAX_VALUE) {
        logger.info("PC is at max value %d, pausing.".formatted((int) pc));
        status = VmStatus.PAUSED;
      } else {
        PCR.set((char) (pc + 1));
      }
    }

    return pc;
  }

  private void executeInstruction(char int16Instruction) {

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
      logger.error("Could not decode instruction 0x%04x (skipping).".formatted((int) int16Instruction));
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
        extraInformation = "PC:%d".formatted((int) PCR.get());
      } catch (Exception e) {
        logger.error("Error generating extra information for error.");
        extraInformation = "(Error generating extra information.)";
      }

      logger.error("Error executing instruction. %s".formatted(extraInformation), ixe);
    }
  }
}
