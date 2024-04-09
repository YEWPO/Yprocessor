#include <svdpi.h>
#include <VTop__Dpi.h>
#include "../include/inst.h"

InstInfo inst_info = {};

void output_inst(int inst, long long dnpc, svLogic valid) {
  inst_info.inst = inst;
  inst_info.dnpc = dnpc;
  inst_info.valid = valid;
}
