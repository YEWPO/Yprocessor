#include <isa.h>
#include "local-include/reg.h"

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

#define NR_REG ARRLEN(regs)

void isa_reg_display() {
  int i;

  for (i = 0; i < NR_REG; i++) {
    printf("%s\t" FMT_WORD "\t", reg_name(i), gpr(i));
    if ((i + 1) % 4 == 0) {
      printf("\n");
    }
  }

  printf("pc\t" FMT_WORD "\n", cpu.pc);
}

word_t isa_reg_str2val(const char *s, bool *success) {
  int i;

  for (i = 0; i < NR_REG; i++) {
    if (strcmp(s, reg_name(i)) == 0) {
      return gpr(i);
    }
  }

  if (strcmp(s, "pc") == 0) {
    return cpu.pc;
  }

  *success = false;
  return 0;
}
