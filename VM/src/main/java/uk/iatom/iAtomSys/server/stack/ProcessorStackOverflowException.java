package uk.iatom.iAtomSys.server.stack;

public class ProcessorStackOverflowException extends Exception {

  public ProcessorStackOverflowException(ProcessorStack processorStack, int pushingValue,
      String message, Throwable cause) {
    super("ProcessorStack overflowed %d while pushing %d: %s".formatted(processorStack.getSizeShorts(),
        pushingValue, message), cause);
  }
}
