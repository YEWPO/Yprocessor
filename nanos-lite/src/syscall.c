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

int fs_open(const char *pathname, int flags, int mode);
size_t fs_read(int fd, void *buf, size_t len);
size_t fs_write(int fd, const void *buf, size_t len);
size_t fs_lseek(int fd, size_t offset, int whence);
int fs_close(int fd);

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
    case SYS_open:
      c->GPRx = fs_open((const char *)a[1], a[2], a[3]);
      break;
    case SYS_read:
      c->GPRx = fs_read(a[1], (void *)a[2], a[3]);
      break;
    case SYS_write:
      c->GPRx = fs_write(a[1], (void *)a[2], a[3]);
      break;
    case SYS_close:
      c->GPRx = fs_close(a[1]);
      break;
    case SYS_lseek:
      c->GPRx = fs_lseek(a[1], a[2], a[3]);
      break;
    case SYS_brk:
      c->GPRx = 0;
      break;
    /* case SYS_execve:
      naive_uload(NULL, (const char *)a[1]);
      break;
    case SYS_gettimeofday:
      sys_gettimeofday((void *)a[1], (void *)a[2]);
      c->GPRx = 0;
      break; */
    default: panic("Unhandled syscall ID = %d", a[0]);
  }

#ifdef STRACE
  const char *fd2filename(int fd);
  if (
      strcmp(syscall_name[a[0]], "open") == 0
      || strcmp(syscall_name[a[0]], "read") == 0
      || strcmp(syscall_name[a[0]], "write") == 0
      || strcmp(syscall_name[a[0]], "close") == 0
      || strcmp(syscall_name[a[0]], "lseek") == 0
      )
    printf("%s (%s, %ld, %ld) = %ld\n", syscall_name[a[0]], fd2filename(a[1]), a[2], a[3], c->GPRx);
  else
    printf("%s (%ld, %ld, %ld) = %ld\n", syscall_name[a[0]], a[1], a[2], a[3], c->GPRx);
#endif
}
