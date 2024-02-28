#include <isa.h>
#include "local-include/csr.h"

const char *csrs[] = {
  [MSTATUS]       = "mstatus",
  [MTVEC]         = "mtvec",
  [MEPC]          = "mepc",
  [MCAUSE]        = "mcause",
};

#define NR_CSR (1 << 12)

word_t isa_csr_str2val(const char *s, bool *success) {
  int i;

  for (i = 0; i < NR_CSR; i++) {
    if (csr_name(i) && strcmp(s, csr_name(i)) == 0) {
      *success = true;
      return csr(i);
    }
  }

  *success = false;
  return 0;
}
