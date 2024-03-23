package uk.iatom.iAtomSys.server.vm.exception;

public class ProcessorStackUnderflowException extends Exception {

  public ProcessorStackUnderflowException(String message, Throwable cause) {
    super("ProcessorStack underflowed: %s".formatted(message), cause);
  }
}
