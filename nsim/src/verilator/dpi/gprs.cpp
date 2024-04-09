#include <svdpi.h>
#include <verilated_dpi.h>
#include "VTop__Dpi.h"
#include <cstdint>

extern "C" void update_gprs(uint64_t* gprs);

void output_gprs(const svOpenArrayHandle gprs) {
  update_gprs((uint64_t*)(((VerilatedDpiOpenVar *)gprs)->datap()));
}
