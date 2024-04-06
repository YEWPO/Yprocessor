#ifndef __NPC_CSR_H__
#define __NPC_CSR_H__

#include <common.h>

static inline int check_csr_idx(int idx) {
  IFDEF(CONFIG_RT_CHECK, assert(idx >= 0 && idx < (1 << 12)));
  return idx;
}

#define csr(idx) (cpu.csr[check_csr_idx(idx)])

static inline const char *csr_name(int idx) {
  extern const char *csrs[];
  return csrs[check_csr_idx(idx)];
}

#endif
