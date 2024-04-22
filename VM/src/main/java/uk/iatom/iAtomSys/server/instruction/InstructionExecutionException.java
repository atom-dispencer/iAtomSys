package uk.iatom.iAtomSys.server.instruction;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import uk.iatom.iAtomSys.common.instruction.Instruction;

public class InstructionExecutionException extends Exception {

  public Instruction instruction;
  public String message;

  public InstructionExecutionException(@Nullable Instruction instruction, @NonNull String message, @Nullable Throwable cause) {
    super(cause);
    this.instruction = instruction;
    this.message = message;
  }

  @Override
  public String getMessage() {
    return "Error executing Instruction '%s': %s".formatted(instruction.name(), message);
  }
}
