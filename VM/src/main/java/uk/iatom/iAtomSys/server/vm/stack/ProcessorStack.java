package uk.iatom.iAtomSys.server.vm.stack;

public interface ProcessorStack {

  void push(int value);

  int pop();
}
