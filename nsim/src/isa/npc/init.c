#include <isa.h>
#include <memory/paddr.h>

static const uint32_t img [] = {
  0x00000297,  // auipc t0,0
  0x00028823,  // sb  zero,16(t0)
  0x0102c503,  // lbu a0,16(t0)
  0x00100073,  // ebreak (used as nsim_trap)
  0xdeadbeef,  // some data
};

void reset_cpu();

static void restart() {
  reset_cpu();
}

void init_isa() {
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));

  restart();
}
