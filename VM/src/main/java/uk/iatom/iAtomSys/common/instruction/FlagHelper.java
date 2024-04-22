package uk.iatom.iAtomSys.common.instruction;

import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterReference;
import uk.iatom.iAtomSys.common.register.RegisterSet;

public class FlagHelper {

  public static RegisterReference oneRegister_02(RegisterSet registerSet, byte flags) {
    byte registerId = (byte) ((flags & 0b11000000) >> 6);
    Register register = registerSet.getRegister(registerId);
    boolean isSelfReference = ((flags & 0b00100000) >> 5) == 1;
    return new RegisterReference(register, isSelfReference);
  }

  public static RegisterReference[] twoRegisters_02_35(RegisterSet registerSet, byte flags) {
    return new RegisterReference[] {
        oneRegister_02(registerSet, flags),
        oneRegister_02(registerSet, (byte) (flags << 3))
    };
  }

}
