package uk.iatom.iAtomSys.server.stack;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutionException;

public class ProcessorStackUnderflowException extends InstructionExecutionException {

  public final ProcessorStack processorStack;

  public ProcessorStackUnderflowException(@NonNull ProcessorStack processorStack,
      @NonNull String message, @Nullable Throwable cause) {
    super(null, message, null);
    this.processorStack = processorStack;
  }

  @Override
  public String getMessage() {
    return "ProcessorStack underflowed. (%s)".formatted(super.getMessage());
  }
}
