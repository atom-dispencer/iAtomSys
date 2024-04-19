package uk.iatom.iAtomSys.client.disassembly;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.instruction.InstructionSet;
import uk.iatom.iAtomSys.common.register.RegisterSet;

@Getter
@Component
public class MemoryDisassembler {

  private final InstructionSet instructionSet;
  private final RegisterSet registerSet;

  @Autowired
  public MemoryDisassembler(InstructionSet instructionSet, RegisterSet registerSet) {
    this.instructionSet = instructionSet;
    this.registerSet = registerSet;
  }

  @Setter
  private int pointer = 0;

  public List<String[]> disassemble(short[] shorts) {
    List<String[]> decoded = new ArrayList<>(shorts.length);
    for (short s : shorts) {
      decoded.add(disassembleInstruction(s));
    }
    return decoded;
  }

  public String[] disassembleInstruction(short instructionShort) {
    byte opcode = Instruction.extractOpcode(instructionShort);
    byte flags = Instruction.extractFlags(instructionShort);
    Instruction instruction = instructionSet.getInstruction(opcode);

    return instruction.disassembler().disassemble(instruction, flags, instructionSet, registerSet);
  }
}
