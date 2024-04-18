package uk.iatom.iAtomSys.client.decode;

import lombok.Getter;
import lombok.Setter;
import uk.iatom.iAtomSys.server.vm.Instructions;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;

@Getter
public class InstructionReader {

  public InstructionReader(byte[] bytes) {
    this.bytes = bytes;
  }

  private final byte[] bytes;

  @Setter
  private int pointer = 0;

  public boolean hasNextByte() {
    return pointer >= 0 && pointer < bytes.length;
  }

  public byte nextByte() {
    return bytes[pointer++];
  }

  public DecodedInstruction readNextInstruction() {
    byte instructionByte = nextByte();
    Instructions instruction = Instructions.fromByte(instructionByte);

    String name = instruction.name();
    String operand1 = "";
    String operand2 = "";

    if (instruction == Instructions.RCP) {
      operand1 = hasNextByte() ? RegisterSet.NAMES.getKey((int) nextByte()) : "???";
      operand2 = hasNextByte() ? RegisterSet.NAMES.getKey((int) nextByte()) : "???";
    } else if (instruction == Instructions.TBH) {
      String msStr = hasNextByte() ? String.format("%02x", nextByte()) : "??";
      String lsStr = hasNextByte() ? String.format("%02x", nextByte()) : "??";
      operand1 = msStr + lsStr;
    }

    return new DecodedInstruction(name, operand1, operand2);
  }
}
