#ifndef __ISA_NPC_H__
#define __ISA_NPC_H__

#include <common.h>

enum {
  MSTATUS     = 0x300,
  MTVEC       = 0x305,
  MEPC        = 0x341,
  MCAUSE      = 0x342,
};

typedef struct {
  word_t gpr[32];
  vaddr_t pc;
  word_t csr[1 << 12];
} npc_CPU_state;

// decode
typedef struct {
  union {
    uint32_t val;
  } inst;
} npc_ISADecodeInfo;

#define isa_mmu_check(vaddr, len, type) (MMU_DIRECT)

#endif
