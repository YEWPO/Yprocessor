#include <common.h>
#include "syscall.h"

const char *syscall_name[] = {
  "exit",
  "yield",
  "open",
  "read",
  "write",
  "kill",
  "getpid",
  "close",
  "lseek",
  "brk",
  "fstat",
  "time",
  "signal",
  "execve",
  "fork",
  "link",
  "unlink",
  "wait",
  "times",
  "gettimeofday",
};

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;

  switch (a[0]) {
    case SYS_exit:
      halt(c->GPR2);
      break;
    case SYS_yield:
      c->GPRx = 0;
      break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }

#ifdef STRACE
  printf("%s (%ld, %ld, %ld) = %ld\n", syscall_name[a[0]], a[1], a[2], a[3], c->GPRx);
#endif
}
