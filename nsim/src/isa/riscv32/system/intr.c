/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NSIM is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>

word_t isa_raise_intr(word_t NO, vaddr_t epc) {
  cpu.csr[MCAUSE] = NO;
  cpu.csr[MEPC] = epc;

#ifdef CONFIG_ETRACE
  log_write("Exception: \n"
      "mcause: " FMT_WORD "\n"
      "mepc: " FMT_WORD "\n", cpu.csr[MCAUSE], cpu.csr[MEPC]);
#endif

  return cpu.csr[MTVEC];
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}
