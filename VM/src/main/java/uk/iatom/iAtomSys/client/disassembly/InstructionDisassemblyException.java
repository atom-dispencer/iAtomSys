package uk.iatom.iAtomSys.client.disassembly;

import org.springframework.lang.Nullable;

public class InstructionDisassemblyException extends Exception {

  public InstructionDisassemblyException(String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
