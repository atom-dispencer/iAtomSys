nasm -f win64 test.asm
gcc -o  test.exe test.obj -nostdlib -lkernel32
