#include <am.h>
#include <nemu.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)

static uint32_t width;
static uint32_t height;

void __am_gpu_init() {
  uint32_t gpu_cfg = inl(VGACTL_ADDR);
  width = gpu_cfg >> 16;
  height = (gpu_cfg << 16) >> 16;
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = width, .height = height,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  uint32_t *pixels = ctl->pixels;

  fb += ctl->y * width + ctl->x;
  for (int j = 0; j < ctl->h; ++j) {
    uint32_t *ptr = fb;
    for (int i = 0; i < ctl->w; ++i) {
      outl((uintptr_t)ptr, *pixels);
      pixels++;
      ptr++;
    }
    fb += width;
  }

  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
