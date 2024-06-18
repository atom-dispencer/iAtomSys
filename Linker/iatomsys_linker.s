.global _start
.intel_syntax noprefix

_start:
  // A shameless copy from Low Level Learning on YouTube, just to check my toolchain is working

  // sys_write
  mov rax,1
  mov rdi,1
  lea rsi,[iatomsys_name]
  mov rdx,9
  syscall

  // sys_exit
  mov rax,69
  mov rdi,69
  syscall

iatomsys_name:
  .asciz "iAtomSys\n"
