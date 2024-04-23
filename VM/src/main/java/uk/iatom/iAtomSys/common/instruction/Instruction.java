package uk.iatom.iAtomSys.common.instruction;

import uk.iatom.iAtomSys.client.disassembly.InstructionDisassembler;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutor;

public record Instruction(
    // Common
    String name, byte opcode,
    // Client
    InstructionDisassembler disassembler,
    // Server
    InstructionExecutor executor
) {

  public static byte extractOpcode(short instruction) {
    return (byte) ((instruction & 0xff00) >> 8);
  }

  public static byte extractFlags(short instruction) {
    return (byte) (instruction & 0x00ff);
  }
}
