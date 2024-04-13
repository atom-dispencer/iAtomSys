package uk.iatom.iAtomSys.server.vm;

import static uk.iatom.iAtomSys.server.vm.Int16Helper.INT_16_MAX;
import static uk.iatom.iAtomSys.server.vm.Int16Helper.bytesToInt16;
import static uk.iatom.iAtomSys.server.vm.Int16Helper.int16ToBytes;

import uk.iatom.iAtomSys.server.vm.exception.InstructionExecutionException;
import uk.iatom.iAtomSys.server.vm.exception.ProcessorStackOverflowException;
import uk.iatom.iAtomSys.server.vm.exception.ProcessorStackUnderflowException;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.Register;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

public enum Instructions {

  // Google-Style would have me wrap these onto the same line,
  // but here I put my foot down. Wrapping them makes them far
  // harder to read and is counterproductive.
  NOP((byte) 0x00, Instructions::xNOP), //
  RCP((byte) 0x01, Instructions::xRCP), //
  TBH((byte) 0x02, Instructions::xTBH), //
  MRD((byte) 0x03, Instructions::xMRD), //
  MWT((byte) 0x04, Instructions::xMWT), //
  PSH((byte) 0x05, Instructions::xPSH), //
  POP((byte) 0x06, Instructions::xPOP), //
  ADD((byte) 0x07, Instructions::xADD), //
  SUB((byte) 0x08, Instructions::xSUB); //

  final byte byteValue;
  final InstructionExecutor executor;
  Instructions(byte byteValue, InstructionExecutor executor) {
    this.byteValue = byteValue;
    this.executor = executor;
  }

  public static Instructions fromByte(byte b) {
    return switch (b) {
      case 0x00 -> Instructions.NOP;
      case 0x01 -> Instructions.RCP;
      case 0x02 -> Instructions.TBH;
      case 0x03 -> Instructions.MRD;
      case 0x04 -> Instructions.MWT;
      case 0x05 -> Instructions.PSH;
      case 0x06 -> Instructions.POP;
      case 0x07 -> Instructions.ADD;
      case 0x08 -> Instructions.SUB;
      default -> throw new IllegalStateException("Unexpected Instruction byte value: " + b);
    };
  }

  /**
   * @param msByte         The most significant argument byte trailing the instruction.
   * @param lsByte         The least significant argument byte trailing the instruction.
   * @param memory         The VM's {@link Memory}
   * @param registerSet    The VM's {@link RegisterSet}
   * @param processorStack The VM's {@link ProcessorStack}
   */
  private static void xNOP(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) {
    // Do nothing (No Operation)
  }

  private static void xTBH(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) {
    int value = Int16Helper.bytesToInt16(msByte, lsByte);
    registerSet.Idk().set(value);
  }

  /**
   * Copy the values in one {@link Register} to another {@link Register} in this
   * {@link RegisterSet}.
   *
   * @param msByte The index of one the origin {@link Register}.
   * @param lsByte The index of the destination {@link Register}.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xRCP(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) {

    Register reg1 = registerSet.fromByte(msByte);
    Register reg2 = registerSet.fromByte(lsByte);

    int value = reg1.get();
    reg2.set(value);
  }

  /**
   * Read an integer value into {@link RegisterSet#IOMemory()} from the memory address in
   * {@link RegisterSet#Pointer()}.
   *
   * @param msByte Unused.
   * @param lsByte Unused.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xMRD(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) {
    int address = registerSet.Pointer().get();

    byte[] bytesFromMemory = memory.read(address, 2);
    int fromMemory = bytesToInt16(bytesFromMemory);

    registerSet.IOMemory().set(fromMemory);
  }

  /**
   * Write the integer value in {@link RegisterSet#IOMemory()} to the address in
   * {@link RegisterSet#Pointer()}.
   *
   * @param msByte Unused.
   * @param lsByte Unused.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xMWT(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) {

    int value = registerSet.IOMemory().get();
    byte[] valueBytes = int16ToBytes(value);

    int address = registerSet.Pointer().get();
    memory.write(address, valueBytes);
  }

  /**
   * Push the value currently in the {@link RegisterSet#IOStack()} {@link Register} onto the
   * {@link ProcessorStack}.
   *
   * @param msByte Unused.
   * @param lsByte Unused.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xPSH(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) throws InstructionExecutionException {

    try {
      int value = registerSet.IOStack().get();
      processorStack.push(value);
    } catch (ProcessorStackOverflowException overflow) {
      throw new InstructionExecutionException(Instructions.PSH, "", overflow);
    }
  }

  /**
   * Pop the integer currently on top of the {@link ProcessorStack} off and place it in the
   * {@link RegisterSet#IOStack()}.
   *
   * @param msByte Unused.
   * @param lsByte Unused.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xPOP(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) throws InstructionExecutionException {

    try {
      int value = processorStack.pop();
      registerSet.IOStack().set(value);
    } catch (ProcessorStackUnderflowException underflow) {
      throw new InstructionExecutionException(Instructions.POP, "", underflow);
    }
  }

  /**
   * Add the value in the {@link RegisterSet#Numeric()} onto that in the
   * {@link RegisterSet#Accumulator()}, retaining the value in the
   * {@link RegisterSet#Accumulator()}.
   *
   * @param msByte Unused.
   * @param lsByte Unused.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xADD(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) throws InstructionExecutionException {

    int accumulator = registerSet.Accumulator().get();
    int numeric = registerSet.Numeric().get();
    int sum = accumulator + numeric;

    int carry = Math.floorDiv(sum, INT_16_MAX);
    int remainder = sum % INT_16_MAX;

    switch (carry) {
      case 0:
        flags.setCarry(false);
        registerSet.Accumulator().set(remainder);
        break;
      case 1:
        flags.setCarry(true);
        registerSet.Accumulator().set(remainder);
        break;
      default:
        throw new InstructionExecutionException(Instructions.ADD,
            "Excessive CarryCount=%d while adding ACC=%d and NUM=%d".formatted(carry, accumulator,
                numeric), null);
    }
  }

  /**
   * Subtract the value in the {@link RegisterSet#Numeric()} from that in the
   * {@link RegisterSet#Accumulator()}, retaining the value in the
   * {@link RegisterSet#Accumulator()}.
   *
   * @param msByte Unused.
   * @param lsByte Unused.
   * @see #xNOP(byte, byte, Memory, RegisterSet, ProcessorStack, Flags)
   */
  private static void xSUB(byte msByte, byte lsByte, Memory memory, RegisterSet registerSet,
      ProcessorStack processorStack, Flags flags) throws InstructionExecutionException {

    int accumulator = registerSet.Accumulator().get();
    int numeric = registerSet.Numeric().get();
    int sub = accumulator - numeric;

    int carry = Math.floorDiv(sub, INT_16_MAX);
    int remainder = sub % INT_16_MAX;

    switch (carry) {
      case 0:
        flags.setCarry(false);
        registerSet.Accumulator().set(remainder);
        break;
      case -1:
        flags.setCarry(true);
        int wrapped = INT_16_MAX + remainder;
        registerSet.Accumulator().set(wrapped);
        break;
      default:
        throw new InstructionExecutionException(Instructions.ADD,
            "Excessive CarryCount=%d while subtracting NUM=%d from ACC=%d".formatted(carry, numeric,
                accumulator), null);
    }
  }

  public interface InstructionExecutor {

    void exec(byte byte1, byte byte2, Memory memory, RegisterSet registerSet,
        ProcessorStack processorStack, Flags flags) throws InstructionExecutionException;
  }
}
