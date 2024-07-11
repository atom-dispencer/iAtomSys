# iAtomSys Assembler (iASM)
| Language | Tools | Dependencies |
| -------- | ----- | ------------ |
| Elixir   |       |              |

## Why Elixir?
Is Elixir the 'best' language for a command-line app?
Probably not!
But I want to try it and I've made an assembler before (see my C# implemention in 
    [**Crimson-RFASM**](https://github.com/atom-dispencer/Crimson-RFASM)), so I can focus on learning the 
    language without having to solve so many other problems at the same time.

And who knows!
Elixir is supposedly great with lightweight parallel tasks, perhaps like assembling many lines of code??
I'm sure it'll be fine... ;)


## Usage
This project uses **mix** and **escript** to produce a binary which can be run on a machine with Erlang/OTP installed:

```
mix escript.build
escript iasm
```

## Example Code
```asm
// Double-slashes are comments


// First, define constants
// Constants take a $NAME, type and value
// The values can be hex, decimal or ASCII strings
$COOL           INT16   57
$MY_CONST_2     INT16   0xFF00
$FUN_STRING     ASCII   "Hello!"


// Second, define macros
// Macros can use the defined constants
// They can also define arguments, which are numbered $1, $2, ..., $n
// Macros act as a copy-paste, like #include in C/C++
@my_macro {
    POP IDK*
}

@2nd_macro {
    ;0123
    INC $1
    DEC $2
    MOV [$FUN_STRING + 1] $MY_CONST_2   // Simple mathematical expressions inside [] will be evaluated
                                        //  at compile-time
}

@goreg {
    MOV $1 PCR*
}

@linc {
    ;$1
    INC TBH*
}


// Third, define labels/code-blocks
// Code-blocks can refer to labels, sublabels, macros and constants
// Each statement must start on a new line
::__main__
    @linc .my_sublabel      // Labels and sublabels can be used as addresses where addresses are accepted
    MOV TBH PCR*            // These two lines are equivalent
    @goto TBH               // ^
    .my_sublabel            // Sublabels are only defined within their parent label

::my_label
    @linc .my_sublabel      // Doesn't work because .my_sublabel is only defined in ::__main__
            .boop           // Indents don't matter
```
