package uk.iatom.iAtomSys.client.disassembly;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.instruction.InstructionSet;
import uk.iatom.iAtomSys.common.register.RegisterSet;

@Getter
@Component
public class MemoryDisassembler {

  private static final Logger logger = LoggerFactory.getLogger(MemoryDisassembler.class);
  private final InstructionSet instructionSet;
  private final RegisterSet registerSet;
  @Setter
  private int pointer = 0;

  @Autowired
  public MemoryDisassembler(InstructionSet instructionSet, RegisterSet registerSet) {
    this.instructionSet = instructionSet;
    this.registerSet = registerSet;
  }

  public List<String[]> disassemble(char[] shorts) {
    List<String[]> decoded = new ArrayList<>(shorts.length);
    for (char s : shorts) {

      try {
        decoded.add(disassembleInstruction(s));
      } catch (InstructionDisassemblyException idx) {
        logger.error("Error disassembling instruction %04x".formatted(s), idx);
        decoded.add(new String[]{"?", "%04x".formatted(s)});
      }
    }
    return decoded;
  }

  public String[] disassembleInstruction(char instructionChar)
      throws InstructionDisassemblyException {

    try {

      // If odd, treat as a 'load-value' command
      if (instructionChar % 2 == 1) {
        String value = "%04x".formatted((int) instructionChar);
        return new String[]{">", value, "IDK"};
      }

      byte opcode = Instruction.extractOpcode(instructionChar);
      byte flags = Instruction.extractFlags(instructionChar);

      Instruction instruction = instructionSet.getInstruction(opcode);
      if (instruction == null) {
        return new String[]{"?", "%04x".formatted(instructionChar)};
      }

      String[] fragments = instruction.disassembler().disassemble(instruction, flags, registerSet);
      Objects.requireNonNull(fragments,
          "Received null fragment array for %02X,%02X (Instruction: %s)".formatted(opcode, flags,
              instruction));
      Assert.isTrue(fragments.length > 0,
          "Expected > 0 fragments, got %s".formatted(fragments.length));

      return fragments;
    } catch (Exception e) {
      throw new InstructionDisassemblyException(
          "Error disassembling Instruction %04X".formatted(instructionChar), e);
    }
  }
}
