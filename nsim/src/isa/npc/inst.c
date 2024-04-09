#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>

void exec_one_cpu();

 int isa_exec_once(Decode *s) {
   exec_one_cpu();
   return 0;
 }
