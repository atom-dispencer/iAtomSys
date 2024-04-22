package uk.iatom.iAtomSys.common.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.iatom.iAtomSys.server.memory.Memory;

public class RegisterSet {

  private final Register[] registers = new Register[4];
  private final Map<String, Integer> names = new HashMap<>();
  private final Memory memory;
  private final short startAddress;

  public RegisterSet(Memory memory, short startAddress) {
    this.memory = memory;
    this.startAddress = startAddress;
  }

  public void createRegister(String name, int id) throws DuplicateRegisterException {

    if (id < 0 || id >= registers.length) {
      throw new IllegalArgumentException("Cannot create Register. ID too large (%d>%d)".formatted(id, registers.length - 1));
    }

    Register register = new Register(name, id, (short) (startAddress + id), memory);

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

  public Register getRegister(int id) {
    if (id < 0 || id >= registers.length)
      return null;

    return registers[id];
  }

  public Register getRegister(String name) {
    if (!names.containsKey(name))
      return null;
    int id = names.get(name);

    return registers[id];
  }
}
