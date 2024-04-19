package uk.iatom.iAtomSys.common.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.iatom.iAtomSys.server.memory.Memory;

public class RegisterSet {

  private final List<Register> registers = new ArrayList<>();
  private final Map<String, Integer> names = new HashMap<>();
  private final short startAddress;

  public RegisterSet(short startAddress) {
    this.startAddress = startAddress;
  }

  public void createRegister(String name, Memory memory) throws DuplicateRegisterException {
    int id = registers.size();
    Register register = new Register(name, id, (short) (startAddress + id), memory);

    if (names.containsKey(name)) {
      Register original = getRegister(name);
      throw new DuplicateRegisterException(original, register);
    }

    registers.add(register);
    names.put(register.getName(), id);
  }

  public Integer getId(String name) {
    return names.getOrDefault(name, null);
  }

  public Register getRegister(int id) {
    if (id < 0 || id >= registers.size())
      return null;

    return registers.get(id);
  }

  public Register getRegister(String name) {
    if (!names.containsKey(name))
      return null;
    int id = names.get(name);

    return registers.get(id);
  }
}
