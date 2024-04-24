package uk.iatom.iAtomSys.common.register;

public class DuplicateRegisterException extends Exception {

  public DuplicateRegisterException(Register original, Register imposter) {
    super("Cannot create Register %s because %s already exists.".formatted(original, imposter));
  }
}
