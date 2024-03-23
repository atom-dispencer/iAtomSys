package uk.iatom.iAtomSys.server.vm.stack;

public interface ProcessorStack {

  void push(byte[] integer);

  void pop();
}
