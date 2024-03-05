#include <proc.h>

#define MAX_NR_PROC 4

static PCB pcb[MAX_NR_PROC] __attribute__((used)) = {};
static PCB pcb_boot = {};
PCB *current = NULL;

void naive_uload(PCB *pcb, const char *filename);
void context_uload(PCB *pcb, const char *filename, char *const argv[], char *const envp[]);

void switch_boot_pcb() {
  current = &pcb_boot;
}

void hello_fun(void *arg) {
  int j = 1;
  while (1) {
    Log("Hello World from Nanos-lite with arg '%p' for the %dth time!", (uintptr_t)arg, j);
    j ++;
    yield();
  }
}

void context_kload(PCB *pcb, void (*entry)(void *), void *arg) {
  pcb->cp = kcontext((Area) { pcb, pcb + 1 }, entry, arg);
}

PCB *alloc_pcb() {
  static int alloc_pcb_idx = 0;
  assert(alloc_pcb_idx < MAX_NR_PROC);

  PCB *new_pcb = &pcb[alloc_pcb_idx];
  alloc_pcb_idx++;

  return new_pcb;
}

PCB *get_current_pcb() {
  return current;
}

Context* schedule(Context *prev) {
  current->cp = prev;

  static int pcb_idx = MAX_NR_PROC - 1;

  while (1) {
    pcb_idx = (pcb_idx + 1) % MAX_NR_PROC;

    if (pcb[pcb_idx].cp != NULL) {
      current = &pcb[pcb_idx];
      break;
    }
  }

  return current->cp;
}

void init_proc() {
  switch_boot_pcb();

  Log("Initializing processes...");

  char *rproc = "/bin/nterm";
  char *argv[] = { rproc, NULL };
  char *envp[] = { NULL };
  context_uload(alloc_pcb(), rproc, argv, envp);

  switch_boot_pcb();
}
