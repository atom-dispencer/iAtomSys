package uk.iatom.iAtomSys.server.instruction;

import static uk.iatom.iAtomSys.common.instruction.FlagHelper.oneRegister_02;
import static uk.iatom.iAtomSys.common.instruction.FlagHelper.twoRegisters_02_35;

import uk.iatom.iAtomSys.common.instruction.FlagHelper;
import uk.iatom.iAtomSys.common.instruction.Instruction;
import uk.iatom.iAtomSys.common.register.Register;
import uk.iatom.iAtomSys.common.register.RegisterReference;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.server.device.IOPort;
import uk.iatom.iAtomSys.server.memory.Memory;
import uk.iatom.iAtomSys.server.stack.ProcessorStack;
import uk.iatom.iAtomSys.server.stack.ProcessorStackOverflowException;
import uk.iatom.iAtomSys.server.stack.ProcessorStackUnderflowException;

public interface InstructionExecutor {

  /**
   * <h4>No Operation</h4>
   * What it says on the tin.
   *
   * @param ignoredVm    The {@link IAtomSysVM} to act on.
   * @param ignoredFlags The flags given in the least significant byte of the instruction.
   */
  static void xNOP(IAtomSysVM ignoredVm, byte ignoredFlags) {
    // Do nothing (No Operation)
  }

  /**
   * <h4>Move</h4>
   * Read an integer value into the given address.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xMOV(IAtomSysVM vm, byte flags) {
    RegisterReference[] registers = twoRegisters_02_35(vm.getRegisterSet(), flags);
    Memory memory = vm.getMemory();

    char originAddress = registers[0].get();
    char destinationAddress = registers[1].get();

    char value = memory.read(originAddress);
    memory.write(destinationAddress, value);
  }

  /**
   * <h4>Set CPU Flag</h4>
   * Push the value onto the top of the stack.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xFLG(IAtomSysVM vm, byte flags) {
    byte bit = (byte) ((flags & 0b11110000) >> 4);
    int newValue = (flags & 0b00001000) >> 3;

    Register flagRegister = Register.FLG(vm.getRegisterSet());
    char flagRegisterValue = flagRegister.get();

    // If the given bit is not equal to the new desired value, flip it, so it is
    if (((flagRegisterValue & 0x8000) >> bit) != newValue) {
      flagRegisterValue ^= (char) (0x8000 >> bit);
    }

    flagRegister.set(flagRegisterValue);
  }

  /**
   * <h4>Push to Stack</h4>
   * Push the value onto the top of the stack.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xPSH(IAtomSysVM vm, byte flags) throws ProcessorStackOverflowException {
    RegisterReference reference = oneRegister_02(vm.getRegisterSet(), flags);
    char address = reference.get();

    char value = vm.getMemory().read(address);
    vm.getProcessorStack().push(value);
  }

  /**
   * <h4>Pop from Stack</h4>
   * Pop the integer currently on top of the {@link ProcessorStack}.
   *
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xPOP(IAtomSysVM vm, byte flags) throws ProcessorStackUnderflowException {
    RegisterReference reference = oneRegister_02(vm.getRegisterSet(), flags);
    char address = reference.get();

    char value = vm.getProcessorStack().pop();
    vm.getMemory().write(address, value);
  }

  /**
   * <h4>Increment</h4>
   * Increment the value.
   *
   * @param flags 0-3: Register to increment.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xINC(IAtomSysVM vm, byte flags) {
    RegisterReference reference = oneRegister_02(vm.getRegisterSet(), flags);
    char address = reference.get();

    char value = vm.getMemory().read(address);
    value += 1;

    // If the new value is the minimum value for a short, the operation overflowed
    if (value == 0) {
      FlagHelper.setFlag(vm.getRegisterSet(), FlagHelper.CARRY, true);
    }

    vm.getMemory().write(address, value);
  }

  /**
   * <h4>Decrement</h4>
   * Decrement the register given by flags 0-3.
   *
   * @param flags 0-3: Register to increment.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xDEC(IAtomSysVM vm, byte flags) {
    RegisterReference reference = oneRegister_02(vm.getRegisterSet(), flags);
    char address = reference.get();

    char value = vm.getMemory().read(address);
    value -= 1;

    // If the new value is the minimum value for a short, the operation overflowed
    if (value == Short.MAX_VALUE) {
      FlagHelper.setFlag(vm.getRegisterSet(), FlagHelper.CARRY, false);
    }

    vm.getMemory().write(address, value);
  }

  /**
   * <h4>Add</h4>
   * Add the value in the given {@link Register} onto that in ACC, storing the value in ACC.
   *
   * @param flags 0-3: Register to add the value of to ACC.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xADD(IAtomSysVM vm, byte flags) throws InstructionExecutionException {
    RegisterReference register = oneRegister_02(vm.getRegisterSet(), flags);
    char address = register.get();
    char value = vm.getMemory().read(address);

    Register ACC = Register.ACC(vm.getRegisterSet());

    char accumulator = ACC.get();
    char sum = (char) (accumulator + value);

    int carries = Math.floorDiv(sum, Character.MAX_VALUE);
    char remainder = (char) (sum % Character.MAX_VALUE);

    switch (carries) {
      case 0:
        FlagHelper.setFlag(vm.getRegisterSet(), FlagHelper.CARRY, false);
        ACC.set(remainder);
        break;
      case 1:
        FlagHelper.setFlag(vm.getRegisterSet(), FlagHelper.CARRY, true);
        ACC.set(remainder);
        break;
      default:
        throw new InstructionExecutionException(null,
            "Excessive CarryCount=%d while adding ACC=%d and NUM=%d".formatted(
                carries, (int) accumulator, (int) value), null);
    }
  }

  /**
   * <h4>Subtract</h4>
   * Subtract the value in the given {@link Register} from that in ACC, storing the value in ACC.
   *
   * @param flags 0-3: Register to subtract the value of from ACC.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xSUB(IAtomSysVM vm, byte flags) throws InstructionExecutionException {
    RegisterReference register = oneRegister_02(vm.getRegisterSet(), flags);
    char address = register.get();
    char value = vm.getMemory().read(address);

    Register ACC = Register.ACC(vm.getRegisterSet());

    char accumulator = ACC.get();
    char result = (char) (accumulator - value);

    int carries = Math.floorDiv(result, Character.MAX_VALUE);
    char remainder = (char) (result % Character.MAX_VALUE);

    switch (carries) {
      case 0:
        FlagHelper.setFlag(vm.getRegisterSet(), FlagHelper.CARRY, false);
        ACC.set(remainder);
        break;
      case 1:
        FlagHelper.setFlag(vm.getRegisterSet(), FlagHelper.CARRY, true);
        char wrapped = (char) (Character.MAX_VALUE + remainder);
        ACC.set(wrapped);
        break;
      default:
        throw new InstructionExecutionException(null,
            "Anti-excessive CarryCount=%d while subtracting NUM=%d from ACC=%d".formatted(
                carries, (int) value, (int) accumulator), null);
    }
  }

  /**
   * <h4>Zero</h4>
   * Set the value in the given Register to zero.
   *
   * @param flags 0-3: Register to subtract the value of from ACC.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xZRO(IAtomSysVM vm, byte flags) {
    RegisterReference reference = oneRegister_02(vm.getRegisterSet(), flags);
    vm.getMemory().write(reference.get(), (char) 0);
  }

  /**
   * <h4>IO-Port Input-Shuffle</h4>
   * Command the given {@link IOPort} to perform a read operation.
   *
   * @param flags 0-1: The ID of the {@link IOPort}.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xPIS(IAtomSysVM vm, byte flags) {
    int index = (flags & 0b11000000) >> 6;
    IOPort port = vm.getPorts()[index];
    port.shuffleInput();
  }

  /**
   * <h4>IO-Port Output-Shuffle</h4>
   * Command the given {@link IOPort} to perform a write operation.
   *
   * @param flags 0-1: The ID of the {@link IOPort}.
   * @see #xNOP(IAtomSysVM, byte)
   */
  static void xPOS(IAtomSysVM vm, byte flags) {
    int index = (flags & 0b11000000) >> 6;
    IOPort port = vm.getPorts()[index];
    port.shuffleOutput();
  }

  /**
   * Perform the steps to execute the instruction with which this {@link InstructionExecutor} is
   * associated.
   *
   * @param vm    The {@link IAtomSysVM} requesting the execution of the associated
   *              {@link Instruction}.
   * @param flags The flag-byte for this {@link Instruction}, from the VM's memorySlice.
   * @throws InstructionExecutionException In case of <i>anticipated</i> errors.
   */
  void exec(IAtomSysVM vm, byte flags) throws InstructionExecutionException;
}
