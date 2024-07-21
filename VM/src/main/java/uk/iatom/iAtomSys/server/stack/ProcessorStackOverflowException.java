package uk.iatom.iAtomSys.server.stack;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutionException;

public class ProcessorStackOverflowException extends InstructionExecutionException {

  public final ProcessorStack processorStack;
  public final char pushingValue;

  public ProcessorStackOverflowException(@NonNull ProcessorStack processorStack,
      @NonNull char pushingValue,
      @NonNull String message, @Nullable Throwable cause) {
    super(null, message, cause);
    this.processorStack = processorStack;
    this.pushingValue = pushingValue;
  }

  @Override
  public String getMessage() {
    return "ProcessorStack overflowed max size %d while pushing %d: %s (%s)"
        .formatted(processorStack.getSizeShorts(), (int) pushingValue, message, super.getMessage());
  }
}
