package uk.iatom.iAtomSys.common.register;

import uk.iatom.iAtomSys.common.instruction.Instruction;

public class DuplicateRegisterException extends Exception {

  public DuplicateRegisterException(Register original, Register imposter) {
    super("Cannot create Register %s because %s already exists.".formatted(original, imposter));
  }
}
