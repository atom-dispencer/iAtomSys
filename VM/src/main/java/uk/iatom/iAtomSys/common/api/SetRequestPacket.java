package uk.iatom.iAtomSys.common.api;

import jakarta.validation.constraints.NotNull;
import java.util.function.Function;

/**
 * @param address The address which you wish to set the value at, in the same format as
 *                assembly/disassembled code. This may include references to registers.
 * @param value   The value to put into the given memorySlice address.
 */
public record SetRequestPacket(@NotNull String address, @NotNull String value) {

  public static final Function<String, String> ERR_BAD_ADDRESS_LENGTH = (address) -> "Address must be 3-4 characters, got: %d".formatted(address.length());
  public static final Function<String, String> ERR_BAD_VALUE_LENGTH = (value) -> "Value must be 3-4 characters, got: %d".formatted(value.length());
  public static final String ERR_ADDRESS_NULL = "Set command address may not be null";
  public static final String ERR_VALUE_NULL = "Set command value may not be null";
  public static final String ERR_ADDRESS_BLANK = "Set command address may not be blank";
  public static final String ERR_VALUE_BLANK = "Set command value may not be blank";

  public SetRequestPacket {
    if (address == null) {
      throw new IllegalStateException(ERR_ADDRESS_NULL);
    }
    if (value == null) {
      throw new IllegalStateException(ERR_VALUE_NULL);
    }

    if (address.isBlank()) {
      throw new IllegalStateException(ERR_ADDRESS_BLANK);
    }
    if (value.isBlank()) {
      throw new IllegalStateException(ERR_VALUE_BLANK);
    }

    address = address.trim().toUpperCase();
    value = value.trim().toUpperCase();

    if (address.length() < 3 || address.length() > 4) {
      throw new IllegalStateException(ERR_BAD_ADDRESS_LENGTH.apply(address));
    }
    if (value.length() < 3 || value.length() > 4) {
      throw new IllegalStateException(ERR_BAD_VALUE_LENGTH.apply(value));
    }
  }
}
