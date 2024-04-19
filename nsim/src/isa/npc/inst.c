#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>
#include <cpu/difftest.h>

void exec_one_cpu();

void update_gprs(uint64_t *gprs) {
  for (int i = 0; i < 32; ++i) {
    gpr(i) = gprs[i];
  }

  gpr(0) = 0;
}

static Decode *ls;

void update_inst(uint32_t inst, uint64_t dnpc, bool kill, bool invalid, bool device) {
  ls->isa.inst.val = inst;
  ls->snpc += 4;

  if (kill) {
    NSIMTRAP(ls->pc, gpr(10));
  }

  if (invalid) {
    INV(ls->pc);
  }

  if (device) {
    difftest_skip_ref();
  }

  ls->dnpc = dnpc;
}

void outtime_inst() {
  Assert(0, "Oops, maybe something wrong with processor!");
}

 int isa_exec_once(Decode *s) {
   ls = s;
   exec_one_cpu();
   return 0;
}
