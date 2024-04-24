package uk.iatom.iAtomSys.client.disassembly;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

  private static String shortToUTF8String(short s) {
    byte[] bytes = new byte[]{(byte) (s >> 8), (byte) (s & 0x00ff)};
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    return StandardCharsets.UTF_8.decode(buffer).toString();
  }

  public List<String[]> disassemble(short[] shorts) {
    List<String[]> decoded = new ArrayList<>(shorts.length);
    for (short s : shorts) {

      try {
        decoded.add(disassembleInstruction(s));
      } catch (InstructionDisassemblyException idx) {
        logger.error("Error disassembling instruction %04x".formatted(s), idx);
        decoded.add(new String[]{"?", "%04x".formatted(s), shortToUTF8String(s)});
      }

    }
    return decoded;
  }

  public String[] disassembleInstruction(short instructionShort)
      throws InstructionDisassemblyException {

    try {
      byte opcode = Instruction.extractOpcode(instructionShort);
      byte flags = Instruction.extractFlags(instructionShort);

      Instruction instruction = instructionSet.getInstruction(opcode);
      Objects.requireNonNull(instruction, "No instruction found for opcode %02X".formatted(opcode));

      String[] fragments = instruction.disassembler().disassemble(instruction, flags, registerSet);
      Objects.requireNonNull(fragments,
          "Received null fragment array for %02X,%02X (Instruction: %s)".formatted(opcode, flags,
              instruction));
      Assert.isTrue(fragments.length > 0,
          "Expected > 0 fragments, got %s".formatted(fragments.length));

      return fragments;
    } catch (Exception e) {

      String stringVal = "??";
      try {
        stringVal = shortToUTF8String(instructionShort);
      } catch (Exception ignored) {
      }

      throw new InstructionDisassemblyException(
          "Error disassembling Instruction %04X (%s)".formatted(instructionShort, stringVal), e);
    }
  }
}
