package uk.iatom.iAtomSys.server.stack;


import lombok.Getter;
import uk.iatom.iAtomSys.server.memory.Memory;

public class ProcessorStack {

  private final Memory memory;
  @Getter
  private final char startAddress;
  @Getter
  private final int sizeShorts;
  private char writePointer = 0;

  public ProcessorStack(Memory memory, char startAddress, int sizeBytes) {
    this.memory = memory;
    this.startAddress = startAddress;
    this.sizeShorts = sizeBytes;
  }

  public void push(char value) throws ProcessorStackOverflowException {
    if (writePointer - startAddress > sizeShorts) {
      throw new ProcessorStackOverflowException(this, value, "", null);
    }

    char startWriteAddress = (char) (startAddress + writePointer);
    memory.write(startWriteAddress, value);
  }

  public char pop() throws ProcessorStackUnderflowException {
    if (writePointer <= 0) {
      throw new ProcessorStackUnderflowException(this, "ProcessorStack is already empty.", null);
    }

    return memory.read(--writePointer);
  }
}
