#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

static char digits[] = "0123456789abcdef";
static int out_target = 0; // 0 is stdout, 1 is buffer
static int base = 0; // out integer base
static int negtive = 0; // 0 is unsigned, 1 is signed

static long number = 0; // the number need to print
static char* string = 0; // the string need to print

static char* buffer = 0; // out buffer

static int ret = 0; // the return value of print function
static size_t out_size = -1; // the number of characters been out

static void output(int ch) {
  if (out_size == 0) {
    return;
  }

  out_size--;

  if (out_target == 0) {
    putch(ch);
  } else {
    *buffer = ch;
    buffer++;
  }
}

static void printf_num() {
  char buf[30] = {'0'};
  int i = number == 0 ? 1 : 0;

  if (negtive == 1) {
    if (number < 0) {
      output('-');
      number = -number;
    }

    while (number) {
      buf[i++] = digits[number % base];
      number /= base;
    }
  } else {
    unsigned long unumber = number;
    while (unumber) {
      buf[i++] = digits[unumber % base];
      unumber /= base;
    }
  }

  while (i > 0) {
    output(buf[--i]);
  }
}

static void print_string() {
  while (*string) {
    output(*string);
    string++;
  }
}

enum {
  T_D,
  T_LD,
  T_S,
  T_X,
  T_LX,
  T_C,
  T_PER,
  T_U,
  T_LU,
  T_P,
};

char *types[] = {
  [T_D] = "d",
  [T_LD] = "ld",
  [T_S] = "s",
  [T_X] = "x",
  [T_LX] = "lx",
  [T_C] = "c",
  [T_PER] = "%",
  [T_U] = "u",
  [T_LU] = "lu",
  [T_P] = "p",
};

#define NR_TYPE (sizeof(types) / sizeof(types[0]))

static void num_print_env(int type) {
  switch (type) {
    case T_D:
    case T_LD:
      base = 10;
      negtive = 1;
      break;
    case T_X:
    case T_LX:
      base = 16;
      negtive = 1;
      break;
    case T_U:
    case T_LU:
      base = 10;
      negtive = 0;
      break;
    case T_P:
      base = 16;
      negtive = 0;
      break;
    default:
      break;
  }
}

static int vprintf(const char *format, va_list ap) {
  ret = 0;

  while (*format) {
    if (*format == '%') {
      format++;

      int type = 0;

      for (; type < NR_TYPE; ++type) {
        if (strncmp(format, types[type], strlen(types[type])) == 0) {
          break;
        }
      }

      switch (type) {
        case T_PER:
          output('%');
          break;
        case T_C:
          number = va_arg(ap, int);
          output(number);
          break;
        case T_S:
          string = va_arg(ap, char*);
          print_string();
          break;
        case T_D:
        case T_X:
        case T_U:
          number = va_arg(ap, int);
          num_print_env(type);
          printf_num();
          break;
        case T_LD:
        case T_LX:
        case T_LU:
        case T_P:
          number = va_arg(ap, long);
          num_print_env(type);
          printf_num();
          break;
        default:
          break;
      }

      if (type != NR_TYPE) {
        ret++;
        format += strlen(types[type]);
        continue;
      }
    }

    output(*format);
    format++;
  }

  out_size = -1;

  return ret;
}

int printf(const char *format, ...) {
  out_target = 0;
  
  va_list ap;
  va_start(ap, format);
  ret = vprintf(format, ap);
  va_end(ap) ;
  
  out_size = -1;

  return ret;
}

int vsprintf(char *out, const char *format, va_list ap) {
  out_target = 1;
  buffer = out;

  vprintf(format, ap);

  *buffer = '\0';
  out_size = -1;

  return ret;
}

int sprintf(char *out, const char *format, ...) {
  out_target = 1;
  buffer = out;

  va_list ap;
  va_start(ap, format);
  ret = vprintf(format, ap);
  va_end(ap) ;

  *buffer = '\0';
  out_size = -1;
  
  return ret;
}

int snprintf(char *out, size_t n, const char *format, ...) {
  if (n == 0) {
    return 0;
  }

  out_target = 1;
  buffer = out;
  out_size = n - 1;

  va_list ap;
  va_start(ap, format);
  ret = vprintf(format, ap);
  va_end(ap) ;

  *buffer = '\0';
  out_size = -1;

  return ret;
}

int vsnprintf(char *out, size_t n, const char *format, va_list ap) {
  if (n == 0) {
    return 0;
  }

  out_target = 1;
  buffer = out;
  out_size = n - 1;

  ret = vprintf(format, ap);

  *buffer = '\0';
  out_size = -1;

  return ret;
}

#endif
