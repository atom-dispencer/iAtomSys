package uk.iatom.iAtomSys.common.instruction;

import java.util.HashMap;
import java.util.Map;
import uk.iatom.iAtomSys.client.disassembly.InstructionDisassembler;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutor;

public class InstructionSet {

  private final Map<String, Byte> names = new HashMap<>();
  private final Instruction[] instructions = new Instruction[256];

  public void createInstruction(
      // Common
      String name, byte opcode,
      // Client
      InstructionDisassembler disassembler,
      // Server
      InstructionExecutor executor
  )
      throws DuplicateInstructionException {
    Instruction instruction = new Instruction(name, opcode, disassembler, executor);

    if (getInstruction(opcode) != null || getOpcode(name) != null) {
      throw new DuplicateInstructionException(getInstruction(opcode), instruction);
    }

    names.put(instruction.name(), instruction.opcode());
    instructions[instruction.opcode()] = instruction;
  }

  public Byte getOpcode(String name) {
    return names.getOrDefault(name, null);
  }

  public Instruction getInstruction(byte opcode) {
    return instructions[opcode];
  }

}
