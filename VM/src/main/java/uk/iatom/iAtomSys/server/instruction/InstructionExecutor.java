package uk.iatom.iAtomSys.server.instruction;

import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.server.stack.ProcessorStack;
import uk.iatom.iAtomSys.server.stack.ProcessorStackOverflowException;
import uk.iatom.iAtomSys.server.stack.ProcessorStackUnderflowException;

public interface InstructionExecutor {

  void exec(IAtomSysVM vm, byte flags) throws InstructionExecutionException;

  private static Register registerFromFlag_0_3(RegisterSet registerSet, byte flags) {
    byte registerId = (byte) ((flags | 0xf0) >> 4);
    return registerSet.getRegister(registerId);
  }

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
  static void xMOV(IAtomSysVM vm, byte ignoredFlags) {
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
    Register register = registerFromFlag_0_3(vm.getRegisterSet(), flags);
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
    Register register = registerFromFlag_0_3(vm.getRegisterSet(), flags);
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
    Register register = registerFromFlag_0_3(vm.getRegisterSet(), flags);
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
    Register register = registerFromFlag_0_3(vm.getRegisterSet(), flags);
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
    Register register = registerFromFlag_0_3(vm.getRegisterSet(), flags);
    register.set((short) 0);
  }
}
