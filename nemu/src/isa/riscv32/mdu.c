#include <common.h>

uint64_t mulh(uint64_t a, uint64_t b) {
  unsigned __int128 val = (__int128)((int64_t)a) * (__int128)((int64_t)b);
  return val >> 64;
}

uint64_t mulhsu(uint64_t a, uint64_t b) {
  unsigned __int128 val = (__int128)((int64_t)a) * (unsigned __int128)b;
  return val >> 64;
}

uint64_t mulhu(uint64_t a, uint64_t b) {
  unsigned __int128 val = (unsigned __int128)a * (unsigned __int128)b;
  return val >> 64;
}

uint64_t divv(uint64_t a, uint64_t b) {
  if (a == 0x8000000000000000 && b == 0xffffffffffffffff) {
    return a;
  }

  if (b == 0) {
    return 0xffffffffffffffff;
  }

  uint64_t val = (int64_t)a / (int64_t)b;

  return val;
}

uint64_t divu(uint64_t a, uint64_t b) {
  if (b == 0) {
    return 0xffffffffffffffff;
  }

  uint64_t val = a / b;

  return val;
}

uint64_t divw(uint64_t a, uint64_t b) {
  if ((uint32_t)a == 0x80000000 && (uint32_t)b == 0xffffffff) {
    return (int32_t)a;
  }

  if ((uint32_t)b == 0) {
    return 0xffffffffffffffff;
  }

  int32_t val = (int32_t)a / (int32_t)b;

  return val;
}

uint64_t divuw(uint64_t a, uint64_t b) {
  if ((uint32_t)b == 0) {
    return 0xffffffffffffffff;
  }

  int32_t val = (uint32_t)a / (uint32_t)b;

  return val;
}

uint64_t rem(uint64_t a, uint64_t b) {
  if ((int64_t)a == 0x8000000000000000 && (int64_t)b == 0xffffffffffffffff) {
    return 0;
  }

  if (b == 0) {
    return a;
  }

  int64_t val = (int64_t)a % (int64_t)b;

  return val;
}

uint64_t remu(uint64_t a, uint64_t b) {
  if (b == 0) {
    return a;
  }

  uint64_t val = a % b;

  return val;
}

uint64_t remw(uint64_t a, uint64_t b) {
  if ((int32_t)a == 0x80000000 && (int32_t)b == 0xffffffff) {
    return 0;
  }

  if ((uint32_t)b == 0) {
    return (int32_t)a;
  }

  int32_t val = (int32_t)a % (int32_t)b;

  return (int64_t)val;
}

uint64_t remuw(uint64_t a, uint64_t b) {
  if ((uint32_t)b == 0) {
    return (int32_t)a;
  }

  int32_t val = (uint32_t)a % (uint32_t)b;

  return val;
}
