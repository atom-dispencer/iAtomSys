package uk.iatom.iAtomSys.server.vm.stack;


import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.Register;

public class InMemoryProcessorStack implements ProcessorStack {

  private Register popRegister;
  private Memory memory;
  private int startAddress;
  private int byteLength;

  public InMemoryProcessorStack(Register popRegister, Memory memory, int startAddress,
      int byteLength) {
    this.popRegister = popRegister;
    this.memory = memory;
    this.startAddress = startAddress;
    this.byteLength = byteLength;
  }

  @Override
  public void push(byte[] integer) {

  }

  @Override
  public void pop() {

  }
}
