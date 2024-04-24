package uk.iatom.iAtomSys.client.disassembly;

import uk.iatom.iAtomSys.common.instruction.FlagHelper;
import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterReference;
import uk.iatom.iAtomSys.common.register.RegisterSet;

@FunctionalInterface
public interface InstructionDisassembler {

  private static String formatRegisterPointer(Register register, boolean isSelfReference) {
    return register.getName() + (isSelfReference ? "*" : "");
  }

  static String[] noFlags(Instruction instruction, byte ignoredFlags,
      RegisterSet ignoredRegisterSet) {
    return new String[]{instruction.name()};
  }

  static String[] oneRegister_02(Instruction instruction, byte flags, RegisterSet registerSet) {
    RegisterReference reference = FlagHelper.oneRegister_02(registerSet, flags);
    Register register = reference.register();

    return new String[]{instruction.name(),
        formatRegisterPointer(register, reference.isSelfReference())};
  }

  static String[] twoRegisters_02_35(Instruction instruction, byte flags, RegisterSet registerSet) {

    RegisterReference[] references = FlagHelper.twoRegisters_02_35(registerSet, flags);

    Register register1 = references[0].register();
    boolean isSelfReference1 = references[0].isSelfReference();

    Register register2 = references[1].register();
    boolean isSelfReference2 = references[1].isSelfReference();

    return new String[]{
        instruction.name(),
        formatRegisterPointer(register1, isSelfReference1),
        formatRegisterPointer(register2, isSelfReference2)
    };
  }

  static String[] dFLG(Instruction instruction, byte flags, RegisterSet ignoredRegisterSet) {
    byte bit = (byte) ((flags & 0b11110000) >> 4);
    boolean value = ((flags & 0b00001000) >> 3) == 1;

    String operand = "%01x".formatted(bit) + (value ? "+" : "-");
    return new String[]{instruction.name(), operand};
  }

  String[] disassemble(Instruction instruction, byte flags, RegisterSet registerSet);
}
