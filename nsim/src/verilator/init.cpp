#include <VTop.h>
#include <verilated.h>

extern "C" void init_verilator() {
  VerilatedContext* contextp = new VerilatedContext;

  VTop* top = new VTop(contextp);

  while (!contextp->gotFinish()) { top->eval(); }
}
