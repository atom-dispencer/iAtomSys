default rel

extern GetStdHandle
extern WriteFile
extern ExitProcess

section .text
global  main

main:
	;   RSP is stack pointer register!
	;   Stack grows downwards in memory, so this is *expanding* the stack by 40 bytes.
	;   reserve shadow space and align stack by 16
	sub rsp, 40

	;    Get STDOUT's handle
	;    -11 is the DWORD for STDOUT
	;    HANDLE is a 64-byte type on x64
	mov  rcx, -11
	call GetStdHandle

	;    BOOL WriteFile(
	;    [in]                 HANDLE       hFile
	;    [in]                 LPCVOID      lpBuffer
	;    [in]                 DWORD        nNumberOfBytesToWrite
	;    [out, optional]      LPDWORD      lpNumberOfBytesWritten
	;    [in, out, optional]  LPOVERLAPPED lpOverlapped
	;    )
	;    Remember that BOOL (not bool) is a 4-byte type!
	;    -
	;    Handle was returned in RAX, so move to RCX
	;    lpBuffer = RIP-relative LEA (default rel)
	;    DWORD nNumberOfBytesToWrite = 32-bit immediate constant length
	;    &lpNumberOfBytesWritten: Address in main's shadow space to recieve the length written
	;    lpOverlapped = NULL; This is a pointer, needs to be qword.
	mov  rcx, rax
	lea  rdx, [msg]
	mov  r8d, msg.len
	lea  r9, [rsp+48]
	mov  qword [rsp + 32], 0
	call WriteFile

	;   Zero out the ECX register, free the shadow space and exit
	xor ecx, ecx
	add rsp, 40
	ret

	; call ExitProcess

	section .data; or section .rdata to put it in a read-only page

msg:
	db   "-- iAtomSys Linker --", 21, 10; including a CR LF newline is a good idea for a line of output text
	.len equ  $-msg; let the assembler calculate the string length
	;    .len is a local label that appends to the most recent non-dot label, so this is msg.len
