package uk.iatom.iAtomSys.common.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.iatom.iAtomSys.server.memory.Memory;

public class RegisterSet {

  /**
   * Is an array so that we can control the indices more easily as changes to the order/ID are
   * breaking for programs running on the VM, and registers are indexed by their numeric ID.
   */
  private final Register[] registers = new Register[8];
  private final Map<String, Integer> names = new HashMap<>();
  private final Memory memory;
  private final char startAddress;

  public RegisterSet(Memory memory, char startAddress) {
    this.memory = memory;
    this.startAddress = startAddress;
  }

  public void createRegister(String name, int id) throws DuplicateRegisterException {

    if (id < 0 || id >= registers.length) {
      throw new IllegalArgumentException(
          "Cannot create Register %s. ID too large (%d>%d)".formatted(name, id,
              registers.length - 1));
    }

    int address = startAddress + id;
    if (address > Character.MAX_VALUE) {
      throw new IllegalArgumentException("Register memory address %d exceeds 16-bit memory space."
          .formatted(address));
    }

    // TODO Check Short overflow when adding
    Register register = new Register(name, id, (char) address, memory);

    if (names.containsKey(name)) {
      Register original = getRegister(name);
      throw new DuplicateRegisterException(original, register);
    }

    registers[id] = register;
    names.put(register.getName(), id);
  }

  public Integer getId(String name) {
    return names.getOrDefault(name, null);
  }

  public Register[] getActiveRegisters() {
    List<Register> result = new ArrayList<>();
    for (Register register : registers) {
      if (register != null) {
        result.add(register);
      }
    }
    return result.toArray(new Register[0]);
  }

  public Register getRegister(int id) {
    if (id < 0 || id >= registers.length) {
      return null;
    }

    return registers[id];
  }

  public Register getRegister(String name) {
    if (!names.containsKey(name)) {
      return null;
    }
    int id = names.get(name);

    return registers[id];
  }
}
