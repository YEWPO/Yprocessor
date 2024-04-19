#include <svdpi.h>
#include <VTop__Dpi.h>
#include "../include/inst.h"

InstInfo inst_info = {};

void output_inst(int inst, long long dnpc, svLogic kill, svLogic invalid, svLogic device, svLogic en) {
  inst_info.inst = inst;
  inst_info.dnpc = dnpc;
  inst_info.kill = kill;
  inst_info.invalid = invalid;
  inst_info.device = device;
  inst_info.en = en;
}
