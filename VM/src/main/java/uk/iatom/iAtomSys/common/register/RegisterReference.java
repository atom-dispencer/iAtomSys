package uk.iatom.iAtomSys.common.register;

public record RegisterReference(Register register, boolean isSelfReference) {

  public char get() {
    return isSelfReference ? register.getAddress() : register().get();
  }
}
