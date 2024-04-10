#include <isa.h>
#include <memory/paddr.h>

uint64_t naddr_read(uint64_t addr, int len) {
  if (in_pmem(addr)) return paddr_read(addr, len);

  Log("naddr_read: addr = 0x%lx, len = %d", addr, len);

  return 0;
}

void naddr_write(uint64_t addr, int len, uint64_t data) {
  if (in_pmem(addr)) {
    paddr_write(addr, len, data);
    return;
  }

  Log("naddr_write: addr = 0x%lx, len = %d, data = 0x%lx", addr, len, data);
}
