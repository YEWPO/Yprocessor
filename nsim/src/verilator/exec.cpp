#include <verilated.h>
#include <verilated_vcd_c.h>
#include <VTop.h>
#include "../include/inst.h"

#define HIGH 1
#define LOW  0

extern VerilatedContext* context;
extern VTop* top;
extern VerilatedVcdC* tfp;

static void step() {
  context->timeInc(1);
  top->clock = HIGH;
  top->eval();
#ifdef CONFIG_WTRACE
  tfp->dump(context->time());
#endif

  context->timeInc(1);
  top->clock = LOW;
  top->eval();
#ifdef CONFIG_WTRACE
  tfp->dump(context->time());
  tfp->flush();
#endif
}

extern "C" void update_inst(uint32_t inst, uint64_t dnpc, bool kill, bool invalid);
extern "C" void outtime_inst();

extern "C" void exec_one_cpu() {
  extern InstInfo inst_info;

  int cycle_cnt = 0;

  while (!inst_info.en) {
    step();
    cycle_cnt++;

    if (cycle_cnt > 100) {
      break;
    }
  }

  if (inst_info.en && cycle_cnt <= 100) {
    update_inst(inst_info.inst, inst_info.dnpc, inst_info.kill, inst_info.invalid);
  } else {
    outtime_inst();
  }

  inst_info.en = false;
}

extern "C" void reset_cpu() {
  top->reset = HIGH;

  for (int i = 0; i < 3; i++) {
    step();
  }

  top->reset = LOW;
}
