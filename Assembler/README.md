# iAtomSys Assembler (iASM)

Haskell.

Because why not :D

It'll be fun!


```
// Constants
// Names start with a $.
$name = 0x0000

// Macros
// Handled by the preprocessor before tokenisation, so arguments are handled
// as a basic find-replace.
#movmacro arg0 arg1 {
    // Arguments are referenced by name
    MOV %arg0 %arg1
}

// Assembly code
MOV     TBH IDK*
#movmacro TBH IDK*
PSH     TBH*
ADD     IDK
```

## Methodology

Much like other compiled languages, such as C, an iASM executable or memory image is
created by first assembling/compiling the source, then linking the resulting objects
together into a single result.

### Assembly
1) Collect a list of the `.iasm` files to assemble
2) For each file:
    a) Tokenise/Lex each line
    b) Parse the token stream, creating strutures meaningful to the compiler
    c) Apply macros and preprocessing steps
    d) Convert the resulting instruction stream to bytes (or rather, int-16s)
    e) Write the result to a `.oasm` file ready for linking.

Assembly/Compilation is independent for different files, and broadly independent for
lines within files, so much of the process can be done in parallel.

### Linking
1) Collect a list of the `.oasm` files to link
2) For each file:
    a) Read the file's index/link-table
    b) Check whether all required symbols are present
    c) Prepare a binary of the unlinked objects
    d) Replace occurances of symbols with their values
    e) Write the result to disk.

Some of the linking process, such as reading file indices, may be done in parallel,
but some operations may be limited to one core? Maybe? We shall see...
