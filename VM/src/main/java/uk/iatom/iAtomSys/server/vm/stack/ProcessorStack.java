package uk.iatom.iAtomSys.server.vm.stack;

import uk.iatom.iAtomSys.server.vm.exception.ProcessorStackOverflowException;
import uk.iatom.iAtomSys.server.vm.exception.ProcessorStackUnderflowException;

public interface ProcessorStack {

  int getSize();

  void push(int value) throws ProcessorStackOverflowException;

  int pop() throws ProcessorStackUnderflowException;
}
