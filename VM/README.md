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
Java was (and still is!) my 'mother-tongue' of programming - if you [_dig back many years_](https://github.com/atom-dispencer/MagiksMostEvile), you'll find my ancient adventures in modding Minecraft!
A virtual machine is a challenge I've never attempted, plus I have some interesting ideas for a user-interface, so I'd like to be able to implement that without too many worries over my tools.