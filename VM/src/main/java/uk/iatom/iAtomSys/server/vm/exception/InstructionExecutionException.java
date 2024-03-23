package uk.iatom.iAtomSys.server.vm.exception;

import uk.iatom.iAtomSys.server.vm.Instructions;

public class InstructionExecutionException extends Exception {

  public InstructionExecutionException(Instructions instruction, String message, Throwable cause) {
    super("Error executing Instruction '%s': %s".formatted(instruction.name(), message), cause);
  }
}
