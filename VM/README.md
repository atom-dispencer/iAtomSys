# iAtomSys Virtual Machine
| Language | Tools         | Dependencies                       |
|----------|---------------|------------------------------------|
| Java     | IntelliJ IDEA | Spring Framework v3.2 (Web, Shell) |
|          | Gradle 8.6    | Lombok                             |

A 16-bit virtual computer at the core of the iAtomSys ecosystem.

## Objectives & Requirements
- The VM's instruction set shall be of uniform byte-width, arguments included.
- The VM shall operate on a 16-bit architecture.
- The VM shall store numbers big-endian'ly.

## Why Java?
Java was (and still is!) my 'mother-tongue' of programming - if you [_dig back many years_](https://github.com/atom-dispencer/MagiksMostEvile), 
you'll find my ancient adventures in modding Minecraft! A virtual machine is a challenge I've never 
attempted, plus I have some interesting ideas for a user-interface, so I'd like to be able to 
implement that without too many worries over my tools.

## Instruction Set
The iAtomSys instruction and register sets are designed with a few requirements intended to simplify
their implementation and reduce 'syntactic sugar' (such as `%` or `[]`, as in x86) in the
assembly code:
- An instruction's operands shall be no wider than 1 integer (16 bits)
  - Unfortunately to be able to be able to move values between registers we need at least an integer
- The opcode for each instruction must be constant
  - This means that instructions may not contain flags in their opcodes

This has the side effect of requiring more registers to accomplish the same goal.

| Instruction       | Assembly | Hex | Arguments                | Function                                                 |
|-------------------|----------|-----|--------------------------|----------------------------------------------------------|
| No Operation      | NOP      | 00  | -                        | Does nothing (effectively a 1-cycle pause)               | 
| Register Transfer | RCP      |     | `Register1`, `Register2` | Swap the values in the given registers                   |
| To Be Honest...   | TBH      |     | `Integer`                | Write the given value to the `IDK` register              |
| Memory Read       | MRD      |     | -                        | Read the value at the address stored in `PTR` into `MEM` |
| Memory Write      | MWT      |     | -                        | Write the value in `MEM` into `PTR`                      |
| Push to Stack     | PSH      |     | -                        | Push the value in `STK` onto the top of the stack        |
| Pop from Stack    | POP      |     | -                        | Pop the value off the top of the stack into `STK`        |
| Add               | ADD      |     | -                        | Add the value in `NUM` to `ACC`                          |
| Subtract          | SUB      |     | -                        | Subtract the value in `NUM` from `ACC`                   |

## Registers

| Register        | Assembly | Hex | Function                                           |
|-----------------|----------|-----|----------------------------------------------------|
| Program Counter | PCR      |     | Store the current address for program execution    | 
| Accumulator     | ACC      |     | The VM/CPU accumulator for mathematical operations | 
| Return          | RTN      |     | Store pointers to function return values           | 
| StackIO         | STK      |     | Store the inputs and outputs of stack operations   | 
| MemoryIO        | MEM      |     | Store the inputs and outputs of memory operations  | 
| Pointer         | PTR      |     | Store memory addresses for memory operations       | 
| Numeric         | NUM      |     | Store secondary values for mathematical operations | 
| I Don't Know... | IDK      |     | No internal VM/CPU function. User-controlled       |
| Flags           | FLG      |     | Holds CPU flags, one bit per flag.                 |

## Flags

| Flag  | Mask |                                                                |
|-------|------|----------------------------------------------------------------|
| Carry | 01   | Active when the accumulator has carried a bit during addition. |


## Example Programs
```
// Double the value in the accumulator

// Registers
ACC: 01
NUM: 00

// Program
RCP ACC NUM
ADD
```
```
// Add two numbers from known memory addresses

// Memory (hex)
0xff00: 0x01  // 500
0xff01: 0xf4
0xff02: 0x00  // 250
0xff03: 0xfa
0xff04: 0x00  // 0
0xff05: 0x00

// Program
TBH ff00      // Read 500 into MEM
RCP IDK PTR
MRD
RCP MEM ACC   // Move 500 into ACC
TBH ff02      // Read 250 into MEM
RCP IDK PTR
MRD
RCP MEM NUM   // Move 250 into NUM
ADD           // Add the value in NUM to that in ACC, and store in ACC
RCP MEM ACC   // Write ACC (750) back to memory
TBH ff04
RCP IDK PTR
MWT
```
```
// Read a value from an unknown memory address

// Memory
ff00: ff  // The address ff02
ff01: 02
ff02: 00  // The number 100
ff03: 64

TBH ff02     // Load ff02 into IDK
RCP IDK PTR  // Swap values in IDK and PTR
MRD          // Read value at value in PTR (ff02) into MEM
// MEM now contains 0064
```