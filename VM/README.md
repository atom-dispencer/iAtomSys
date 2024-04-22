# iAtomSys Virtual Machine
| Language | Tools         | Dependencies                       |
|----------|---------------|------------------------------------|
| Java     | IntelliJ IDEA | Spring Framework v3.2 (Web, Shell) |
|          | Gradle 8.6    | Lombok                             |

A 16-bit virtual computer at the core of the iAtomSys ecosystem.

## Objectives & Requirements
- The VM shall operate on a 16-bit architecture.
- No single executable action by the virtual CPU shall require the reading of more than 1 integer.
- The VM shall store numbers big-endian'ly.

## Why Java?
Java was (and still is!) my 'mother-tongue' of programming - if you [_dig back many years_](https://github.com/atom-dispencer/MagiksMostEvile), 
  you'll find my ancient adventures in modding Minecraft! A virtual machine is a challenge I've never 
  attempted, plus I have some interesting ideas for a user-interface, so I'd like to be able to 
  implement that without too many worries over my tools.

## Instruction Set
### 'Addressing' the requirements ;)
To satisfy the requirement that _'No single executable action by the virtual CPU shall require the 
  reading of more than 1 integer'_, some instruction gymnastics are required.
To address all 65KiB of memory available to a 16-bit architecture (without serial operations), it
  must be possible to use 16-bit addresses... _or is it?_
By addressing shorts (16-bits) rather than bytes (8-bits), only a 15-bit address is required to
  reach all the installed memory, freeing the 16th bit.

### Instruction Format
The virtual CPU will interpret one 16-bit integer at a time:
```
  Instruction
  .... .... .... ... 0
  ^-------^ ^------^ ^ IA flag (set to Instruction-mode)
  ^         ^ Flags (numbered 0-6)
  ^ Opcode
        
  Address (15-bit value)
  .... .... .... ... 1
  ^----------------^ ^ IA flag (set to Address-mode)
  ^ Address
```
If the integer is odd (contains a 1 in its least significant bit), it will be copied into the least-
  significant 15 bits of the IDK register and left-shifted once.
If the integer is even, it will be interpreted as one of the instructions denoted below.

Many `Register` flags are followed by a `Register*` flag. If the `Register*` flag is enabled, the
  true 16-bit memory address of the register should be used, rather than the register's contents.

| Instruction    | Assembly | Opcode | Flags                                                                | Function                                                                |
|----------------|----------|--------|----------------------------------------------------------------------|-------------------------------------------------------------------------|
| No Operation   | NOP      | 00     |                                                                      | Does nothing (effectively a 1-cycle pause).                             | 
| Move           | MOV      |        | `0-1: Register1`, `2: Register1*`, `3-4: Register2`, `5: Register2*` | Move the value at `Address1` into `Address2`.                           |
| Set Flag       | FLG      |        | `0-3: 4-bit ID`, `4: On/Off`                                         | Set the given bit (0-15) in the FLG hidden-register to the given value. |
| Push to Stack  | PSH      |        | `0-1: Register`, `2: Register*`                                      | Push the value at the given address onto the top of the CPU stack.      |
| Pop from Stack | POP      |        | `0-1: Register`, `2: Register*`                                      | Pop the value off the top of the stack into the given address.          |
| Increment      | INC      |        | `0-1: Register`, `2: Register*`                                      | Increment the integer stored at the given address by 1.                 |
| Decrement      | DEC      |        | `0-1: Register`, `2: Register*`                                      | Decrement the integer stored at the given address by 1.                 |
| Add            | ADD      |        | `0-1: Register`, `2: Register*`                                      | Add the value at the given address to `ACC`.                            |
| Subtract       | SUB      |        | `0-1: Register`, `2: Register*`                                      | Subtract the value at the given address from `ACC`.                     |
| Zero           | ZRO      |        | `0-1: Register`, `2: Register*`                                      | Set the value at the given address to 0.                                |

## Registers

Registers in the VM are stored in memory and can therefore be accessed through standard memory
  addresses. This is, however, inadvisable and generally impractical.
Bear in mind that the whole state of the VM is stored in memory, so 'Registers' are just an
  abstraction for a known constant memory address.
Registers can be passed as flags to instructions via their 2-bit address.

| Register          | Assembly | Hex | Function                                           |
|-------------------|----------|-----|----------------------------------------------------|
| Accumulator       | ACC      | 00  | The VM/CPU accumulator for mathematical operations | 
| I Don't Know...   | IDK      | 01  | No internal VM/CPU function. User-controlled       |
| To Be Honest...   | TBH      | 02  | No internal VM/CPU function. User-controlled       |
| Laugh Out Loud... | LOL      | 03  | No internal VM/CPU function. User-controlled       |

### Hidden Registers
Several registers are hidden, meaning that they cannot be addressed directly from instructions.
Interactions with these must happen indirectly via other instructions.

| Register        | Hex | Function                                         |
|-----------------|-----|--------------------------------------------------|
| Program Counter | 04  | Stores the current address for program execution | 
| Flags           | 05  | Stores CPU flags.                                | 

## Flags
CPU flags are stored in the invisible register FLG.

| Flag  | Bit | Mask |                                                                |
|-------|-----|------|----------------------------------------------------------------|
| Carry | 1   | 0001 | Active when the accumulator has carried a bit during addition. |


## Example Programs
```
// Double the value in the accumulator

// Registers
ACC: 01
IDK: 00

// Program
RCP ACC IDK
ADD IDK
```
```
// Add two numbers from known memory addresses

// Memory (hex)
0xff00: 01 f4  // 500
0xff01: 00 fa  // 250
0xff02: 00 00  // 0

// Program
ff01          // Read ff01 into IDK (want ff00 but must be odd)
DEC IDK       // Decrement IDK ff01 -> ff00
MRD IDK LOL   // ^
RCP MEM ACC   // Move 500 into ACC

ff01          // Move ff01 into IDK (it's odd, so no changes!)
RCP IDK       // Read "250" from ff01 into MEM
MRD IDK       // ^

ADD MEM       // Add the value in MEM to that in ACC, storing the result in ACC
RCP ACC MEM   // Write ACC (750) back to memory
ff03          // Want to write to ff02, but that's even!
DEC IDK       // ^
RCP IDK PTR   // Write to ff02
MWT           // ^
```
```
// Read a value from an unknown memory address

// Memory
ff00: ff 02  // The address ff02
ff01: 00 64  // The number 100

ff01         // Load ff02 into IDK
INC IDK      // ^
RCP IDK PTR  // Copy IDK to PTR
MRD          // Read value ("100") at value in PTR (ff02) into MEM
// MEM now contains 0064
```