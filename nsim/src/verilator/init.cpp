#include <VTop.h>
#include <memory>
#include <verilated.h>
#include <verilated_vcd_c.h>

extern "C" void init_verilator(const char* trace_file) {
  const std::unique_ptr<VerilatedContext> contextp{new VerilatedContext};
  VTop* top = new VTop;

  Verilated::traceEverOn(true);
  VerilatedVcdC* tfp = new VerilatedVcdC;
  top->trace(tfp, 99);
  tfp->open(trace_file == NULL ? "build/trace.vcd" : trace_file);
}
