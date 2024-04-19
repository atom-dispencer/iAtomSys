package uk.iatom.iAtomSys.server.stack;

public class ProcessorStackUnderflowException extends Exception {

  public ProcessorStackUnderflowException(String message) {
    this(message, null);
  }

  public ProcessorStackUnderflowException(String message, Throwable cause) {
    super("ProcessorStack underflowed: %s".formatted(message), cause);
  }
}
