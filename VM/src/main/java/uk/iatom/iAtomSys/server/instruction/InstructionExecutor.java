package uk.iatom.iAtomSys.server.instruction;

import static uk.iatom.iAtomSys.common.instruction.FlagHelper.oneRegister_02;
import static uk.iatom.iAtomSys.common.instruction.FlagHelper.twoRegisters_02_35;

import uk.iatom.iAtomSys.common.register.RegisterReference;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.server.memory.Memory;
import uk.iatom.iAtomSys.server.stack.ProcessorStack;

public interface InstructionExecutor {

  void exec(IAtomSysVM vm, byte flags) throws InstructionExecutionException;


  /**
   * @param ignoredVm The {@link IAtomSysVM} to act on.
   * @param ignoredFlags The flags given in the least significant byte of the instruction.
   */
  static void xNOP(IAtomSysVM ignoredVm, byte ignoredFlags) {
    // Do nothing (No Operation)
  }

  /**
   * Read an integer value into the given address.
   *
   */
  static void xMOV(IAtomSysVM vm, byte flags) {
    RegisterReference[] registers = twoRegisters_02_35(vm.getRegisterSet(), flags);
    Memory memory = vm.getMemory();

    short originAddress = registers[0].get();
    short destinationAddress = registers[1].get();

    short value = memory.read(originAddress);
    memory.write(destinationAddress, value);
  }

  /**
   * Push the value onto the top of the stack.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xFLG(IAtomSysVM vm, byte flags) {
    byte bit = (byte) ((flags & 0b11110000) >> 4);
    int newValue = (flags & 0b00001000) >> 3;

    Register flagRegister = Register.FLG(vm.getRegisterSet());
    short flagRegisterValue = flagRegister.get();

    // If the given bit is not equal to the new desired value, flip it, so it is
    if (((flagRegisterValue & 0x8000) >> bit) != newValue) {
      flagRegisterValue ^= (short) (0x8000 >> bit);
    }

    flagRegister.set(flagRegisterValue);
  }

  /**
   * Push the value onto the top of the stack.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xPSH(IAtomSysVM vm, byte ignoredFlags) throws InstructionExecutionException {
  }

  /**
   * Pop the integer currently on top of the {@link ProcessorStack}.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xPOP(IAtomSysVM vm, byte ignoredFlags) throws InstructionExecutionException {
  }

  /**
   * Increment the value.
   *
   * @param flags 0-3: Register to increment.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xINC(IAtomSysVM vm, byte flags) {
    Register register = oneRegister_02(vm.getRegisterSet(), flags);
    short current = register.get();
    register.set((short) (current + 1));
  }

  /**
   * Decrement the register given by flags 0-3.
   *
   * @param flags 0-3: Register to increment.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xDEC(IAtomSysVM vm, byte flags) {
    Register register = oneRegister_02(vm.getRegisterSet(), flags);
    short current = register.get();
    register.set((short) (current - 1));
  }


  /**
   * Add the value in the given {@link Register} onto that in ACC, storing the value in ACC.
   *
   * @param flags 0-3: Register to add the value of to ACC.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xADD(IAtomSysVM vm, byte flags) throws InstructionExecutionException {
    Register register = oneRegister_02(vm.getRegisterSet(), flags);
    short target = register.get();

    Register ACC = Register.ACC(vm.getRegisterSet());

    short accumulator = ACC.get();
    short sum = (short) (accumulator + target);

    short carry = (short) Math.floorDiv(sum, Short.MAX_VALUE);
    short remainder = (short) (sum % Short.MAX_VALUE);

    switch (carry) {
      case 0:
        vm.getFlags().setCarry(false);
        ACC.set(remainder);
        break;
      case 1:
        vm.getFlags().setCarry(true);
        ACC.set(remainder);
        break;
      default:
        throw new InstructionExecutionException(null,
            "Excessive CarryCount=%d while adding ACC=%d and NUM=%d".formatted(carry, accumulator,
                target), null);
    }
  }

  /**
   * Subtract the value in the given {@link Register} from that in ACC, storing the value in ACC.
   *
   * @param flags 0-3: Register to subtract the value of from ACC.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xSUB(IAtomSysVM vm, byte flags) throws InstructionExecutionException {
    Register register = oneRegister_02(vm.getRegisterSet(), flags);
    short target = register.get();

    Register ACC = Register.ACC(vm.getRegisterSet());

    short accumulator = ACC.get();
    short sub = (short) (accumulator - target);

    short carry = (short) Math.floorDiv(sub, Short.MAX_VALUE);
    short remainder = (short) (sub % Short.MAX_VALUE);

    switch (carry) {
      case 0:
        vm.getFlags().setCarry(false);
        ACC.set(remainder);
        break;
      case -1:
        vm.getFlags().setCarry(true);
        short wrapped = (short) (Short.MAX_VALUE + remainder);
        ACC.set(wrapped);
        break;
      default:
        throw new InstructionExecutionException(null,
            "Excessive CarryCount=%d while subtracting NUM=%d from ACC=%d".formatted(carry, target,
                accumulator), null);
    }
  }

  /**
   * Set the value in the given Register to zero.
   *
   * @param flags 0-3: Register to subtract the value of from ACC.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xZRO(IAtomSysVM vm, byte flags) {
    Register register = oneRegister_02(vm.getRegisterSet(), flags);
    register.set((short) 0);
  }
}
