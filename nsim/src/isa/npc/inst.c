#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>

void exec_one_cpu();

void update_gprs(uint64_t *gprs) {
  for (int i = 0; i < 32; ++i) {
    gpr(i) = gprs[i];
  }

  gpr(0) = 0;
}

static Decode *ls;

void update_inst(uint32_t inst, uint64_t dnpc) {
  ls->isa.inst.val = inst;
  ls->snpc += 4;
  ls->dnpc = dnpc;
}

 int isa_exec_once(Decode *s) {
   ls = s;
   exec_one_cpu();
   return 0;
 }
