/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
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

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static char buf[65536] = {};
static char code_buf[65536 + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";

static char *sbuf;
static int t;

static uint32_t choose(uint32_t n) {
  return rand() % n;
}

static void gen_num() {
  switch (choose(2)) {
    case 0:
      sprintf(sbuf, "%u", choose(50000));
      break;
    case 1:
      sprintf(sbuf, "0x%x", choose(50000));
      break;
  }
  sbuf += strlen(sbuf);
}

static void gen_rand_op() {
  switch (choose(15)) {
    case 0:
      sprintf(sbuf, "+");
      break;
    case 1:
      sprintf(sbuf, "-");
      break;
    case 2:
      sprintf(sbuf, "*");
      break;
    case 3:
      sprintf(sbuf, "/");
      break;
    case 4:
      sprintf(sbuf, ">");
      break;
    case 5:
      sprintf(sbuf, "<");
      break;
    case 6:
      sprintf(sbuf, ">=");
      sbuf++;
      break;
    case 7:
      sprintf(sbuf, "<=");
      sbuf++;
      break;
    case 8:
      sprintf(sbuf, "==");
      sbuf++;
      break;
    case 9:
      sprintf(sbuf, "!=");
      sbuf++;
      break;
    case 10:
      sprintf(sbuf, "&");
      break;
    case 11:
      sprintf(sbuf, "|");
      break;
    case 12:
      sprintf(sbuf, "^");
      break;
    case 13:
      sprintf(sbuf, "&&");
      sbuf++;
      break;
    case 14:
      sprintf(sbuf, "||");
      sbuf++;
      break;
    default:
      break;
  }

  sbuf++;
}

static void gen_rand_unary_op() {
  switch (choose(2)) {
    case 0:
      sprintf(sbuf, "!");
      break;
    case 1:
      sprintf(sbuf, "~");
      break;
    default:
      break;
  }
  sbuf++;
}

static void gen(char c) {
  sprintf(sbuf, "%c", c);
  sbuf++;
}

static void gen_rand_space() {
  int n = choose(2);

  while (n--) {
    sprintf(sbuf, " ");
    sbuf++;
  }
}

static void gen_rand_expr() {
  if (t > 1000) {
    gen_num();
    return;
  }
  t++;

  gen_rand_space();
  switch (choose(4)) {
    case 0:
      gen_num();
      break;
    case 1:
      gen('(');
      gen_rand_expr();
      gen(')');
      break;
    case 2:
      gen_rand_expr();
      gen_rand_op();
      gen_rand_expr();
      break;
    case 3:
      gen_rand_unary_op();
      gen_num();
    default:
      break;
  }
  gen_rand_space();
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i ++) {
    sbuf = buf;
    t = 0;
    gen_rand_expr();
    *sbuf = '\0';

    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc -Werror /tmp/.code.c -o /tmp/.expr");
    if (ret != 0) continue;

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    fscanf(fp, "%d", &result);
    pclose(fp);

    printf("%u %s\n", result, buf);
  }
  return 0;
}
