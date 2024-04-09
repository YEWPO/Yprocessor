#include <cstdint>
#include <svdpi.h>
#include <VTop__Dpi.h>

#define BUS_WIDTH 64

extern "C" uint64_t naddr_read(uint64_t addr, int len);
extern "C" void naddr_write(uint64_t addr, int len, uint64_t data);

long long read_mem(long long addr, svLogic en) {
  if (!en) return 0;
  return naddr_read(addr, BUS_WIDTH / 8);
}

void write_mem(long long addr, long long data, char strb) {
  if (!strb) return;

  long long read_data = naddr_read(addr, BUS_WIDTH / 8);
  long long byte_mask = (1 << 8) - 1;

  for (int i = 0; i < BUS_WIDTH / 8; ++i) {
    if (!(strb & (1 << i))) continue;

    long long op_mask = byte_mask << (i << 3);

    read_data = (read_data & ~op_mask) | (data & op_mask);
  }

  naddr_write(addr, 8, read_data);
}
