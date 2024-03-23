package uk.iatom.iAtomSys.server.vm.stack;


import uk.iatom.iAtomSys.server.vm.Int16Helper;
import uk.iatom.iAtomSys.server.vm.exception.ProcessorStackOverflowException;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.Register;

public class Int16InMemoryProcessorStack implements ProcessorStack {

  private Register popRegister;
  private Memory memory;
  private int startAddress;
  private int sizeBytes;
  private int writePointer = 0;

  public Int16InMemoryProcessorStack(Register popRegister, Memory memory, int startAddress,
      int sizeBytes) {
    this.popRegister = popRegister;
    this.memory = memory;
    this.startAddress = startAddress;
    this.sizeBytes = sizeBytes;
  }

  @Override
  public int getSize() {
    return sizeBytes;
  }

  @Override
  public void push(int integer) throws ProcessorStackOverflowException {
    int startWriteAddress = startAddress + writePointer;
    if (startWriteAddress >= Int16Helper.INT_16_MAX) {
      throw new ProcessorStackOverflowException(this, integer, "", null);
    }
  }

  @Override
  public int pop() {
    return 0;
  }
}
