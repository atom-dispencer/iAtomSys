package uk.iatom.iAtomSys.client.disassembly;

import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.instruction.InstructionSet;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.common.register.Register;

@FunctionalInterface
public interface InstructionDisassembler {
  String[] disassemble(Instruction instruction, byte flags, InstructionSet instructionSet, RegisterSet registerSet);

  static String[] noFlags(Instruction instruction, byte ignoredFlags, InstructionSet ignoredInstructionSet, RegisterSet ignoredRegisterSet) {
    return new String[] { instruction.name(), "" };
  }

  static String[] register_0_3(Instruction instruction, byte flags, InstructionSet instructionSet, RegisterSet registerSet) {
    byte registerId = (byte) ((flags | 0xf0) >> 4);
    Register register = registerSet.getRegister(registerId);
    return new String[] { instruction.name(), register.getName() };
  }

  static String[] twoRegisters(Instruction instruction, byte flags, InstructionSet instructionSet, RegisterSet registerSet) {
    boolean useRegisterAddress1 = (flags & 0b00100000) > 0;
    byte register1Index = (byte) ((flags | 0b11000000) >> 6);
    Register register1 = registerSet.getRegister(register1Index);

    boolean useRegisterAddress2 = (flags & 0b00100000) > 0;
    byte register2Index = (byte) ((flags | 0b11000000) >> 6);
    Register register2 = registerSet.getRegister(register2Index);

    String registerStr1 = useRegisterAddress1 ? register1.getName() + "*" : register1.getName();
    String registerStr2 = useRegisterAddress2 ? register2.getName() + "*" : register2.getName();

    return new String[] { instruction.name(), registerStr1, registerStr2 };
  }
}
