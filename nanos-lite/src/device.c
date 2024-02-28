#include <common.h>
#include <sys/time.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = {
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) {
  size_t nwrite = 0;
  const char *pbuf = buf;

  while (len > 0) {
    putch(*pbuf);
    pbuf++;
    len--;
    nwrite++;
  }

  return nwrite;
}

size_t events_read(void *buf, size_t offset, size_t len) {
  AM_INPUT_KEYBRD_T kbd;
  ioe_read(AM_INPUT_KEYBRD, &kbd);

  if (kbd.keycode == AM_KEY_NONE) {
    return 0;
  }

  size_t nread;
  if (kbd.keydown == 1) {
    nread = snprintf(buf, len, "kd %s\n", keyname[kbd.keycode]);
  } else {
    nread = snprintf(buf, len, "ku %s\n", keyname[kbd.keycode]);
  }

  return nread;
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  return 0;
}

size_t fb_write(const void *buf, size_t offset, size_t len) {
  return 0;
}

void timeofday(void *tv, void* tz) {
  assert(tv != NULL && tz == NULL);

  struct timeval *ptr = tv;
  AM_TIMER_UPTIME_T uptime;
  ioe_read(AM_TIMER_UPTIME, &uptime);
  ptr->tv_sec = uptime.us / 1000000;
  ptr->tv_usec = uptime.us % 1000000;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
