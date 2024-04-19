package uk.iatom.iAtomSys.server.stack;


import lombok.Getter;
import uk.iatom.iAtomSys.server.memory.Memory;

public class ProcessorStack {

  private final Memory memory;
  @Getter private final short startAddress;
  @Getter private final int sizeShorts;
  private short writePointer = 0;

  public ProcessorStack(Memory memory, short startAddress, int sizeBytes) {
    this.memory = memory;
    this.startAddress = startAddress;
    this.sizeShorts = sizeBytes;
  }

  public void push(short value) throws ProcessorStackOverflowException {
    if (writePointer - startAddress > sizeShorts) {
      throw new ProcessorStackOverflowException(this, value, "", null);
    }

    short startWriteAddress = (short) (startAddress + writePointer);
    memory.write(startWriteAddress, value);
  }

  public short pop() throws ProcessorStackUnderflowException {
    if (writePointer <= 0) {
      throw new ProcessorStackUnderflowException("ProcessorStack is already empty.");
    }

    return memory.read(--writePointer);
  }
}
