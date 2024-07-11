package uk.iatom.iAtomSys.common;

public class AddressFormatException extends Exception {
  private String message;

  public AddressFormatException(String message) {
    super(message);
  }
}
