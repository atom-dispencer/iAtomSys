package uk.iatom.iAtomSys.common.instruction;

public class DuplicateInstructionException extends Exception {

  public DuplicateInstructionException(Instruction original, Instruction imposter) {
    super("Cannot create Instruction %s because %s already exists.".formatted(original, imposter));
  }
}
