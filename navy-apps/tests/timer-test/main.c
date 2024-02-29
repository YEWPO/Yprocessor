#include <stdio.h>
#include <stdint.h>
#include <NDL.h>

uint32_t boot_time = 0;

void init_time() {
  boot_time = NDL_GetTicks();
}

uint32_t get_time() {
  uint32_t now_time;
  now_time = NDL_GetTicks();
  return now_time - boot_time;
}

int main() {
  uint64_t sec = 1;
  while (1) {
    while (get_time() / 500 < sec);
    printf("time pass %ld half sec.\n", sec);
    sec++;
  }
  return 0;
}
