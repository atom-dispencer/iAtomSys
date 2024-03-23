package uk.iatom.iAtomSys.server.vm;

public class InstructionExecutionException extends Exception {

  public InstructionExecutionException(Instructions instruction, String message, Throwable cause) {
    super("Error executing Instruction '%s': %s".formatted(instruction.name(), message), cause);
  }
}
