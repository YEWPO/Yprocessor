#include <isa.h>
#include <memory/paddr.h>

uint64_t naddr_read(uint64_t addr, int len) {
  return paddr_read(addr, len);
}

void naddr_write(uint64_t addr, int len, uint64_t data) {
  paddr_write(addr, len, data);
}
