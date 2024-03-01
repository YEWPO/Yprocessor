#include <proc.h>

#define MAX_NR_PROC 4

static PCB pcb[MAX_NR_PROC] __attribute__((used)) = {};
static PCB pcb_boot = {};
PCB *current = NULL;

void naive_uload(PCB *pcb, const char *filename);
void context_uload(PCB *pcb, const char *filename);

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

void init_proc() {
  switch_boot_pcb();

  Log("Initializing processes...");

  context_uload(&pcb[0], "/bin/pal");
  context_kload(&pcb[1], hello_fun, (void *)2);
  switch_boot_pcb();

}

Context* schedule(Context *prev) {
  current->cp = prev;

  static int pcb_idx = 0;

  while (1) {
    pcb_idx = (pcb_idx + 1) % MAX_NR_PROC;

    if (pcb[pcb_idx].cp != NULL) {
      current = &pcb[pcb_idx];
      break;
    }
  }

  return current->cp;
}
