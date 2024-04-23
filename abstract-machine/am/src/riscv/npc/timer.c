#include <am.h>
#include ISA_H

#define RTC_ADDR 0xa0000048

static uint64_t boot_time;

#define READ_RTC ((uint64_t)inl(RTC_ADDR + 4) << 32) | (uint64_t)inl(RTC_ADDR)

void __am_timer_init() {
  boot_time = READ_RTC;
}

void __am_timer_uptime(AM_TIMER_UPTIME_T *uptime) {
  uint64_t now_time = READ_RTC;
  uptime->us = (now_time - boot_time) * 37 / 100;
}

void __am_timer_rtc(AM_TIMER_RTC_T *rtc) {
  rtc->second = 0;
  rtc->minute = 0;
  rtc->hour   = 0;
  rtc->day    = 0;
  rtc->month  = 0;
  rtc->year   = 1900;
}
