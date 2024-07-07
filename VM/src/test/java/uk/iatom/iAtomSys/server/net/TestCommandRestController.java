package uk.iatom.iAtomSys.server.net;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.iatom.iAtomSys.server.net.CommandRestController.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.iatom.iAtomSys.common.api.DebugSymbols;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.VmStatus;
import uk.iatom.iAtomSys.common.register.DuplicateRegisterException;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.IAtomSysVM;
import uk.iatom.iAtomSys.server.memory.Memory;

public class TestCommandRestController {

  @Mock
  IAtomSysVM vm;
  @Mock
  Memory memory;

  RegisterSet registerSet;
  CommandRestController rest;

  @BeforeEach
  void setup_each() {
    MockitoAnnotations.openMocks(this);

    when(memory.read(anyChar())).thenReturn(Character.MAX_VALUE);
    doNothing().when(memory).write(anyChar(), anyChar());
    registerSet = new RegisterSet(memory, (char) 0);

    when(vm.getRegisterSet()).thenReturn(registerSet);
    when(vm.getMemory()).thenReturn(memory);
    rest = new CommandRestController(vm);
  }

  @Test
  void hello() {
    assertEquals(HELLO_WORLD, rest.hello());
  }

  @Test
  void set_running() {
    when(vm.getStatus()).thenReturn(VmStatus.RUNNING);
    assertEquals(ERR_NOT_ALLOWED_VM_RUNNING, rest.set(null));
  }

  @ParameterizedTest
  @ValueSource(strings = { "0000", "0123", "4567", "890a", "bcde", "ffff"})
  void set_good_addresses(String address) {
    SetRequestPacket packet = new SetRequestPacket(address, "0000");
    assertDoesNotThrow(() -> rest.set(packet));
  }

  @ParameterizedTest
  @ValueSource(strings = { "0000", "0123", "4567", "890a", "bcde", "ffff"})
  void set_good_values(String value) {
    SetRequestPacket packet = new SetRequestPacket("0000", value);
    assertDoesNotThrow(() -> rest.set(packet));
  }

  @ParameterizedTest
  @ValueSource(strings = { "000Z" })
  void set_strange_addresses(String address) {
    SetRequestPacket packet = new SetRequestPacket(address, "0000");
    String result = rest.set(packet);
    assertEquals(ERR_NUMBER_FORMAT(address), result);
  }

  @ParameterizedTest
  @ValueSource(strings = { "000Z" })
  void set_strange_values(String value) {
    SetRequestPacket packet = new SetRequestPacket("0000", value);
    String result = rest.set(packet);
    assertEquals(ERR_NUMBER_FORMAT(value), result);
  }

  @ParameterizedTest
  @ValueSource(strings = { "00000", "000" })
  void set_long_addresses(String address) {
    SetRequestPacket packet = new SetRequestPacket(address, "0000");
    String result = rest.set(packet);
    assertEquals(ERR_INPUT_LENGTH(address), result);
  }

  @ParameterizedTest
  @ValueSource(strings = { "00000", "000" })
  void set_long_values(String value) {
    SetRequestPacket packet = new SetRequestPacket("0000", value);
    String result = rest.set(packet);
    assertEquals(ERR_INPUT_LENGTH(value), result);
  }

  @Test
  void set_success() throws DuplicateRegisterException {
    registerSet.createRegister("TEST", 6);

    String addressStr = "TEST";
    String valueStr = "TEST";
    char address = 6;
    char value = 0xFFFF;

    SetRequestPacket packet = new SetRequestPacket(addressStr, valueStr);
    String message = rest.set(packet);

    verify(memory, times(1)).write(anyChar(), anyChar());
    verify(memory, times(1)).read(anyChar());
    Assertions.assertEquals(SET_SUCCESS(addressStr, address, valueStr, value), message);
  }

  @Test
  void drop_debug_running() {
    when(vm.getStatus()).thenReturn(VmStatus.RUNNING);
    assertEquals(ERR_NOT_ALLOWED_VM_RUNNING, rest.dropDebug());
  }

  @Test
  void drop_debug_success() {
    DebugSymbols debugSymbols = DebugSymbols.empty();

    doCallRealMethod().when(vm).setDebugSymbols(any());
    when(vm.getDebugSymbols()).thenCallRealMethod();

    vm.setDebugSymbols(debugSymbols);
    assertEquals(debugSymbols, vm.getDebugSymbols());
    String message = rest.dropDebug();
    // == is important because we're making sure the backing object has changed.
    assertNotSame(debugSymbols, vm.getDebugSymbols());
    assertTrue(debugSymbols.isEmpty());

    assertEquals(DROP_DEBUG_SUCCESS(DebugSymbols.EMPTY_NAME), message);
  }
}
