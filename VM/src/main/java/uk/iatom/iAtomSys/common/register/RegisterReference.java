package uk.iatom.iAtomSys.common.register;

public record RegisterReference(Register register, boolean isSelfReference) {

  public short get() {
    return isSelfReference ? register.getAddress() : register().get();
  }
}
