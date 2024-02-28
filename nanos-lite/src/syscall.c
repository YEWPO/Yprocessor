#include <common.h>
#include "syscall.h"

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;

  switch (a[0]) {
    case SYS_exit:
      halt(c->GPR2);
      break;
    case SYS_yield:
      c->GPRx = 0;
      break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
