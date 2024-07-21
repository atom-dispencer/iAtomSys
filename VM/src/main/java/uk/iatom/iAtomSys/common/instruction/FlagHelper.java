package uk.iatom.iAtomSys.common.instruction;

import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterReference;
import uk.iatom.iAtomSys.common.register.RegisterSet;

public class FlagHelper {

  public static final int CARRY = 0;
  public static final int IO0 = 8;
  public static final int IO1 = 9;
  public static final int IO2 = 10;
  public static final int IO3 = 11;

  public static final int FLAGS_COUNT = 16;

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
    char flagRegisterValue = flagRegister.get();

    // If the given bit is not equal to the new desired value, flip it, so it is
    if (((flagRegisterValue & 0x8000) >> flagId) == 1 != value) {
      flagRegisterValue ^= (char) (0x8000 >> flagId);
    }

    flagRegister.set(flagRegisterValue);
  }

  public static boolean getFlag(RegisterSet registerSet, int flagId) {
    Register flagRegister = Register.FLG(registerSet);
    return getFlag(flagId, flagRegister.get());
  }

  public static boolean getFlag(int flagId, char flags) {
    return (flags & (0x8000 >> flagId)) > 0;
  }

  public enum Flag {
    CARRY(FlagHelper.CARRY),
    // Spare
    F01(1),
    F02(2),
    F03(3),
    F04(4),
    F05(5),
    F06(6),
    F07(7),
    // IO Ports
    IO0(FlagHelper.IO0),
    IO1(FlagHelper.IO1),
    IO2(FlagHelper.IO2),
    IO3(FlagHelper.IO3),
    // More spares
    F12(12),
    F13(13),
    F14(14),
    F15(15);

    public final int bitIndex;

    Flag(int bitIndex) {
      this.bitIndex = bitIndex;
    }

    public static Flag fromBitIndex(int i) {
      if (i < 0 || i >= FLAGS_COUNT) {
        throw new IllegalArgumentException("Maximum flag index is %d, got %d".formatted(FLAGS_COUNT, i));
      }
      return Flag.values()[i];
    }
  }
}
