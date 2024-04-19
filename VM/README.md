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
If the integer is even, it will be interpreted as one of the instructions denoted below:

| Instruction       | Assembly | Opcode | Flags           | Function                                                                                                                                                                    |
|-------------------|----------|--------|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| No Operation      | NOP      | 00     |                 | Does nothing (effectively a 1-cycle pause).                                                                                                                                 | 
| Register Transfer | RCP      |        |                 | Read the integer in `IDK`. Decode the most significant byte as `Register1` and the least significant byte as `Register2`. Copy the integer in `Register1` into `Register2`. |
| Memory Read       | MRD      |        |                 | Read the value at the address stored in `PTR` into `MEM`.                                                                                                                   |
| Memory Write      | MWT      |        |                 | Write the value in `MEM` to the address stored in `PTR`.                                                                                                                    |
| Push to Stack     | PSH      |        |                 | Push the value in `STK` onto the top of the stack.                                                                                                                          |
| Pop from Stack    | POP      |        |                 | Pop the value off the top of the stack into `STK`.                                                                                                                          |
| Increment         | INC      |        | `0-3: Register` | Increment the value in `Register` by 1.                                                                                                                                     |
| Decrement         | DEC      |        | `0-3: Register` | Decrement the value in `Register` by 1.                                                                                                                                     |
| Add               | ADD      |        | `0-3: Register` | Add the value in `Register` to `ACC`.                                                                                                                                       |
| Subtract          | SUB      |        | `0-3: Register` | Subtract the value in `Register` from `ACC`.                                                                                                                                |
| Zero              | ZRO      |        | `0-3: Register` | Set the value in `Register` to 0.                                                                                                                                           |

## Registers

Registers in the VM are stored in memory and can therefore be accessed through standard memory
  addresses. This is, however, inadvisable and generally impractical.


### Addressable Registers
Addressable registers can be passed as flags to instructions via their 4-bit address.

| Register        | Assembly | Hex | Function                                           |
|-----------------|----------|-----|----------------------------------------------------|
| Program Counter | PCR      |     | Stores the current address for program execution   | 
| Flags           | FLG      |     | Holds CPU flags, one bit per flag.                 |
| Accumulator     | ACC      |     | The VM/CPU accumulator for mathematical operations | 
| Return          | RTN      |     | Store pointers to function return values           | 
| StackIO         | STK      |     | Store the inputs and outputs of stack operations   | 
| MemoryIO        | MEM      |     | Store the inputs and outputs of memory operations  | 
| Pointer         | PTR      |     | Store memory addresses for memory operations       | 
| Numeric         | NUM      |     | Store secondary values for mathematical operations | 
| I Don't Know... | IDK      |     | No internal VM/CPU function. User-controlled       |

## Flags

| Flag  | Mask |                                                                |
|-------|------|----------------------------------------------------------------|
| Carry | 01   | Active when the accumulator has carried a bit during addition. |


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
RCP IDK PTR   // Read "500" from ff00 into MEM
MRD           // ^
RCP MEM ACC   // Move 500 into ACC

ff01          // Move ff01 into IDK (it's odd, so no changes!)
RCP IDK PTR   // Read "250" from ff01 into MEM
MRD           // ^
RCP MEM NUM   // Move 250 into NUM

ADD           // Add the value in NUM to that in ACC, storing the result in ACC
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