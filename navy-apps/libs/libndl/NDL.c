#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <fcntl.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;
static int offset_w = 0, offset_h = 0;

static uint64_t boot_time = 0;

uint32_t NDL_GetTicks() {
  uint64_t now_time;
  struct timeval tv;
  gettimeofday(&tv, NULL);
  now_time = tv.tv_sec * 1000000 + tv.tv_usec;
  return (now_time - boot_time) / 1000;
}

int NDL_PollEvent(char *buf, int len) {
  size_t n = read(evtdev, buf, len);
  return !!n;
}

void NDL_OpenCanvas(int *w, int *h) {
  int fd = open("/proc/dispinfo", 0, 0);
  char buf[64];
  read(fd, buf, 64);
  sscanf(buf, "WIDTH:%d\nHEIGHT:%d\n", &screen_w, &screen_h);
  close(fd);
  if (*w == 0 && *h == 0) {
    *w = screen_w, *h = screen_h;
  }
  offset_w = (screen_w - *w) / 2;
  offset_h = (screen_h - *h) / 2;

  if (getenv("NWM_APP")) {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    write(fbctl, buf, len);
    while (1) {
      // 3 = evtdev
      int nread = read(3, buf, sizeof(buf) - 1);
      if (nread <= 0) continue;
      buf[nread] = '\0';
      if (strcmp(buf, "mmap ok") == 0) break;
    }
    close(fbctl);
  } else {
    fbdev = open("/dev/fb", 0, 0);
  }
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  uint32_t *ptr = pixels;
  int pos = (y + offset_h) * screen_w + x + offset_w;

  for (int i = 0; i < h; ++i) {
    lseek(fbdev, pos * 4, SEEK_SET);
    write(fbdev, ptr, w * 4);

    ptr += w;
    pos += screen_w;
  }
}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  } else {
    evtdev = open("/dev/events", 0, 0);
  }

  struct timeval tv;
  gettimeofday(&tv, NULL);
  boot_time = tv.tv_sec * 1000000 + tv.tv_usec;

  return 0;
}

void NDL_Quit() {
  close(evtdev);
}
