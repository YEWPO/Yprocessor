#include <common.h>
#include "syscall.h"
#include <proc.h>

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
void timeofday(void *tv, void* tz);
void context_uload(PCB *pcb, const char *filename, char *const argv[], char *const envp[]);
PCB *get_current_pcb();
void switch_boot_pcb();
static char *argv[64] = { NULL };
static char *envp[64] = { NULL };

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;

#ifdef STRACE
  const char *fd2filename(int fd);
  if (strcmp(syscall_name[a[0]], "read") == 0
      || strcmp(syscall_name[a[0]], "write") == 0
      || strcmp(syscall_name[a[0]], "close") == 0
      || strcmp(syscall_name[a[0]], "lseek") == 0)
    printf("%s (\"%s\", %ld, %ld) ", syscall_name[a[0]], fd2filename(a[1]), a[2], a[3]);
  else if (strcmp(syscall_name[a[0]], "execve") == 0)
    printf("%s (\"%s\", %ld, %ld) ", syscall_name[a[0]], (char *)a[1], a[2], a[3]);
  else
    printf("%s (%ld, %ld, %ld) ", syscall_name[a[0]], a[1], a[2], a[3]);
#endif

  PCB *current_pcb = get_current_pcb();

  switch (a[0]) {
    case SYS_exit:
      argv[0] = "/bin/nterm";
      argv[1] = NULL;
      context_uload(current_pcb, "/bin/nterm", argv, envp);
      switch_boot_pcb();
      yield();
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
    case SYS_execve:
      if (fs_open((const char *)a[1], a[2], a[3]) >= 0) { 
        context_uload(current_pcb, (const char *)a[1], (char *const*)a[2], (char *const*)a[3]);
        switch_boot_pcb();
        yield();
      }
      c->GPRx = -2;
      break;
    case SYS_gettimeofday:
      timeofday((void *)a[1], (void *)a[2]);
      c->GPRx = 0;
      break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }

#ifdef STRACE
  printf("= %d\n", c->GPRx);
#endif
}
