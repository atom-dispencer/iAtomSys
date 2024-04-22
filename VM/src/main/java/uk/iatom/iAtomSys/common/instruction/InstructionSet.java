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

    // No need to check opcode as is restrained to 0-255 anyway
    Instruction instruction = new Instruction(name, opcode, disassembler, executor);

    if (getInstruction(opcode) != null || getOpcode(name) != null) {
      throw new DuplicateInstructionException(getInstruction(opcode), instruction);
    }

    // All good to go!
    names.put(instruction.name(), instruction.opcode());

    // Bytes are signed in Java, but negative indices will error!
    int index = Byte.toUnsignedInt(instruction.opcode());
    instructions[index] = instruction;
  }

  public Byte getOpcode(String name) {
    return names.getOrDefault(name, null);
  }

  public Instruction getInstruction(byte opcode) {
    return instructions[Byte.toUnsignedInt(opcode)];
  }

}
