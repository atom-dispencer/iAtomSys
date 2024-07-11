package uk.iatom.iAtomSys.common;

public class AddressFormatException extends NumberFormatException {
  public AddressFormatException(String message) {
    super(message);
  }

  public AddressFormatException(NumberFormatException nfx) {
    this(nfx.getMessage());
  }
}
