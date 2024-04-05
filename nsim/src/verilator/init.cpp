#include <verilated.h>

extern "C" void init_verilator() {
  VerilatedContext* contextp = new VerilatedContext;

  while (!contextp->gotFinish()) {}
}
