#include <verilated.h>
#include <verilated_vcd_c.h>
#include <VTop.h>

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
#endif
}

extern "C" void exec_one_cpu() {
  step();
}

extern "C" void reset_cpu() {
  top->reset = HIGH;

  for (int i = 0; i < 3; i++) {
    step();
  }

  top->reset = LOW;
}
