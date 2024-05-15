package uk.iatom.iAtomSys.common.instruction;

import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterReference;
import uk.iatom.iAtomSys.common.register.RegisterSet;

public class FlagHelper {

  public static final int CARRY = 0;

  public static RegisterReference oneRegister_02(RegisterSet registerSet, byte flags) {
    byte registerId = (byte) ((flags & 0b11000000) >> 6);
    Register register = registerSet.getRegister(registerId);
    boolean isSelfReference = ((flags & 0b00100000) >> 5) == 1;
    return new RegisterReference(register, isSelfReference);
  }

  public static RegisterReference[] twoRegisters_02_35(RegisterSet registerSet, byte flags) {
    return new RegisterReference[]{
        oneRegister_02(registerSet, flags),
        oneRegister_02(registerSet, (byte) (flags << 3))
    };
  }

  public static void setFlag(RegisterSet registerSet, int flagId, boolean value) {
    Register flagRegister = Register.FLG(registerSet);
    short flagRegisterValue = flagRegister.get();

    // If the given bit is not equal to the new desired value, flip it, so it is
    if (((flagRegisterValue & 0x8000) >> flagId) == 1 != value) {
      flagRegisterValue ^= (short) (0x8000 >> flagId);
    }

    flagRegister.set(flagRegisterValue);
  }

  public static boolean getFlag(RegisterSet registerSet, int flagId) {
    Register flagRegister = Register.FLG(registerSet);
    int flagRegisterValue = flagRegister.get(); // Implicit upcast to int

    return (flagRegisterValue & (0x8000 >> flagId)) > 0;
  }

  public enum Flag {
    CARRY(0),
    DEV_0(8),
    DEV_1(9),
    DEV_2(10),
    DEV_3(11);

    public final int bitIndex;

    Flag(int bitIndex) {
      this.bitIndex = bitIndex;
    }
  }
}
