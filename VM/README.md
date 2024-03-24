# iAtomSys Virtual Machine
| Language | Tools         | Dependencies                       |
| -------- | ------------- | ---------------------------------- |
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
| Instruction       | Assembly | Hex | Arguments                | Function                                                 |
|-------------------|----------|-----|--------------------------|----------------------------------------------------------|
| No Operation      | NOP      | 00  | -                        |                                                          | 
| Register Transfer | RTX      | 01  | `Register1`, `Register2` |                                                          |
| To Be Honest...   | TBH      | 02  | `Integer`                | Write the given value to the `IDK` register.             |
| Memory Read       | MRD      | 03  | -                        | Read the value at the address stored in `PTR` into `MEM` |
| Memory Write      | MWT      | 04  | -                        | Write the value in `MEM` into `PTR`                      |
| Push to Stack     | PSH      | 05  | -                        |                                                          |
| Pop from Stack    | POP      | 06  | -                        |                                                          |
| Add               | ADD      | 07  | -                        |                                                          |
| Subtract          | SUB      | 08  | -                        |                                                          |

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


## Example Programs
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
RTX IDK PTR
MRD
RTX MEM ACC   // Move 500 into ACC
TBH ff02      // Read 250 into MEM
RTX IDK PTR
MRD
RTX MEM NUM   // Move 250 into NUM
ADD           // Add the value in NUM to that in ACC, and store in ACC
RTX MEM ACC   // Write ACC (750) back to memory
TBH ff04
RTX IDK PTR
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
RTX IDK PTR  // Swap values in IDK and PTR
MRD          // Read value at value in PTR (ff02) into MEM
// MEM now contains 0064
```