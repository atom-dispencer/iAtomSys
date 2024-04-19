package uk.iatom.iAtomSys.server.instruction;

import uk.iatom.iAtomSys.common.instruction.Instruction;

public class InstructionExecutionException extends Exception {

  public Instruction instruction;
  public String message;

  public InstructionExecutionException(Instruction instruction, String message, Throwable cause) {
    super(cause);
    this.instruction = instruction;
    this.message = message;
  }

  @Override
  public String getMessage() {
    return "Error executing Instruction '%s': %s".formatted(instruction.name(), message);
  }
}
