package uk.iatom.iAtomSys.server.vm.exception;

import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

public class ProcessorStackOverflowException extends Exception {

  public ProcessorStackOverflowException(ProcessorStack processorStack, int pushingValue,
      String message, Throwable cause) {
    super("ProcessorStack overflowed %d while pushing %d: %s".formatted(processorStack.getSize(),
        pushingValue, message), cause);
  }
}
