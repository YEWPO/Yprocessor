#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  size_t length = 0;

  while (s[length] != '\0') {
    length++;
  }

  return length;
}

char *strcpy(char *dst, const char *src) {
  size_t i = 0;

  while (src[i] != '\0') {
    dst[i] = src[i];
    i++;
  }

  dst[i] = '\0';

  return dst;
}

char *strncpy(char *dst, const char *src, size_t n) {
  size_t i = 0;

  while (i < n && src[i] != '\0') {
    dst[i] = src[i];
    i++;
  }

  if (i != n) {
    dst[i] = '\0';
  }

  return dst;
}

char *strcat(char *dst, const char *src) {
  size_t i = 0;
  while (dst[i] != '\0') {
    i++;
  }

  size_t j = 0;

  while (src[j] != '\0') {
    dst[i] = src[j];
    i++;
    j++;
  }

  dst[i] = '\0';

  return dst;
}

int strcmp(const char *s1, const char *s2) {
  size_t i = 0;

  while (s1[i] != '\0' && s2[i] != '\0' && s1[i] == s2[i]) {
    i++;
  }

  return s1[i] - s2[i];
}

int strncmp(const char *s1, const char *s2, size_t n) {
  size_t i = 0;

  while (i < n && s1[i] != '\0' && s2[i] != '\0' && s1[i] == s2[i]) {
    i++;
  }

  return i == n ? 0 : s1[i] - s2[i];
}

void *memset(void *s, int c, size_t n) {
  char *ptr = (char*)s;

  for (int i = 0; i < n; ++i) {
    ptr[i] = c;
  }

  return ptr;
}

void *memmove(void *dst, const void *src, size_t n) {
  const char *s = src;
  char *d = dst;

  size_t i;

  if (dst <= src) {
    i = 0;
    while (i < n) {
     d[i] = s[i];
     i++;
    }
  } else {
    i = n;
    while (i > 0) {
      d[i - 1] = s[i - 1];
      i--;
    }
  }

  return d;
}

void *memcpy(void *out, const void *in, size_t n) {
  size_t i = 0;

  const char *s = in;
  char *d = out;

  while (i < n) {
    d[i] = s[i];
    i++;
  }

  return d;
}

int memcmp(const void *s1, const void *s2, size_t n) {
  const char *p1 = s1;
  const char *p2 = s2;
  size_t i = 0;

  while (i < n && p1[i] != '\0' && p2[i] != '\0' && p1[i] == p2[i]) {
    i++;
  }

  return i == n ? 0 : p1[i] - p2[i];
}

#endif
