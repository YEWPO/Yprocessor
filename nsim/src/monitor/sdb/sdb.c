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
#include <cpu/cpu.h>
#include <memory/paddr.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"

static int is_batch_mode = false;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nsim) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}


static int cmd_q(char *args) {
  nsim_state.state = NSIM_QUIT;
  return -1;
}

static int cmd_si(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");

  if (arg == NULL) {
    /*
     * no argument given
     * default step one instruction exactly
     */
    cpu_exec(1);
  } else {
    uint64_t step_time;
    char *endptr;
    step_time = strtoull(arg, &endptr, 10);

    if (*endptr == '\0') {
      /*
       * got a valid value
       * execute step_time times
       */
      cpu_exec(step_time);
    } else {
      /* got a invalid value */
      printf("invalid value %s\n", arg);
    }
  }

  return 0;
}

static int cmd_info(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");

  if (arg == NULL) {
    /*
     * no argument given
     * output help
     */

    printf("r - List of integer registers and their contents, for selected stack frame.\n");
    printf("w - Status of specified watchpoints (all watchpoints if no argument).\n");
  } else {
    /*
     * check arg
     * and execute
     */
    if (strcmp("r", arg) == 0) {
      isa_reg_display();
      return 0;
    }
    if (strcmp("w", arg) == 0) {
      print_watchpoints();
      return 0;
    }

    printf("Unknown option '%s'\n", arg);
  }

  return 0;
}

static int cmd_x(char *args) {
  /* extract tow arguments */
  char *arg1 = strtok(NULL, " ");
  char *arg2 = strtok(NULL, " ");

  if (arg1 == NULL || arg2 == NULL) {
    /* lack of argument */
    printf("Lack of arguments\n");
  } else {
    /* get step time value */
    uint64_t xsize;
    char *endptr;
    xsize = strtoull(arg1, &endptr, 10);

    if (*endptr == '\0') {
      /* valid value of N*/
      word_t paddr = strtoull(arg2, &endptr, 16);

      if (*endptr == '\0') {
        /* valid value of EXPR */
        int i;

        for (i = 0; i < xsize; i++) {
          word_t xaddr = paddr + i * 4;
          printf(ANSI_FMT(FMT_WORD, ANSI_FG_BLUE) ": 0x%08lx\n", xaddr, paddr_read(xaddr, 4));
        }
      } else {
        printf("invalid address: %s\n", arg2);
      }
    } else {
      printf("invalid value of N: %s\n", arg1);
    }
  }

  return 0;
}

static int cmd_p(char *args) {
  if (args == NULL) {
    /* not provide EXPR */
    printf("no expression\n");
  } else {
    /* provide EXPR */

    /* parse EXPR */
    bool flag;
    word_t val = expr(args, &flag);

    if (flag == false) {
      printf("invalid expression\n");
    } else {
      printf("HEX: " FMT_WORD "\tDEC: " "%" PRIu64 "\n",val, val);
    }
  }

  return 0;
}

static int cmd_w(char *args) {
  if (args == NULL) {
    /* not provide EXPR */
    printf("no expression\n");
  } else {
    /* provide EXPR */
    new_wp(args);
  }

  return 0;
}

static int cmd_d(char *args) {
  /* extract first argument */
  char *arg = strtok(NULL, " ");

  if (arg == NULL) {
    printf("Lack of argument.\n");
  } else {
    uint32_t no;
    char *endptr;
    no = strtoul(arg, &endptr, 10);

    if (*endptr == '\0') {
      /* valid value */
      free_wp(no);
    } else {
      printf("invalid argument.\n");
    }
  }

  return 0;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NSIM", cmd_q },
  {"si", "Step one or [N] instruction exactly.", cmd_si},
  {"info", "Generic command for showing things about the program being debugged." , cmd_info},
  {"x", "Examine memory: x/FMT ADDRESS.", cmd_x},
  {"p", "Print value of expression EXP.", cmd_p},
  {"w", "Set a watchpoint for EXPRESSION.", cmd_w},
  {"d", "Delete a watchpoint.", cmd_d},
};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}
