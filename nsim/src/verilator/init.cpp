#include <VTop.h>
#include <verilated.h>
#include <verilated_vcd_c.h>

VTop* top;
VerilatedVcdC* tfp;
VerilatedContext* context;

extern "C" void init_verilator(const char* trace_file) {
  context = new VerilatedContext;
  top = new VTop;
  tfp = new VerilatedVcdC;

#ifdef CONFIG_WTRACE
  Verilated::traceEverOn(true);
  top->trace(tfp, 99);
  tfp->open(trace_file == NULL ? "build/trace.vcd" : trace_file);
#endif
}
