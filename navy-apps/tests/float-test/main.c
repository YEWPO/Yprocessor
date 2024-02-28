#include <fixedptc.h>
#include <assert.h>
#include <stdio.h>

void test_muli() {
  printf("float_muli_test\n");
  fixedpt expected = fixedpt_rconst(3.9);
  fixedpt result = fixedpt_muli(fixedpt_rconst(1.3), 3);
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_muli2() {
  printf("float_muli_test\n");
  fixedpt expected = fixedpt_rconst(-3.9);
  fixedpt result = fixedpt_muli(fixedpt_rconst(1.3), -3);
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_divi() {
  printf("float_divi_test\n");
  fixedpt expected = fixedpt_rconst(0.65);
  fixedpt result = fixedpt_divi(fixedpt_rconst(1.3), 2);
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_divi2() {
  printf("float_divi_test\n");
  fixedpt expected = fixedpt_rconst(-0.65);
  fixedpt result = fixedpt_divi(fixedpt_rconst(1.3), -2);
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_mul() {
  printf("float_mul_test\n");
  fixedpt expected = fixedpt_rconst(3.445);
  fixedpt result = fixedpt_mul(fixedpt_rconst(1.3), fixedpt_rconst(2.65));
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_div() {
  printf("float_div_test\n");
  fixedpt expected = fixedpt_rconst(0.4905660377358491);
  fixedpt result = fixedpt_div(fixedpt_rconst(1.3), fixedpt_rconst(2.65));
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_abs() {
  printf("float_abs_test\n");
  fixedpt expected = fixedpt_rconst(9.85);
  fixedpt result = fixedpt_abs(fixedpt_rconst(-9.85));
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_floor() {
  printf("float_floor_test\n");
  fixedpt expected = fixedpt_rconst(8.0);
  fixedpt result = fixedpt_floor(fixedpt_rconst(8.85));
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

void test_ceil() {
  printf("float_ceil\n");
  fixedpt expected = fixedpt_rconst(10.0);
  fixedpt result = fixedpt_ceil(fixedpt_rconst(9.111));
  printf("expected: %s\n", fixedpt_cstr(expected, 2));
  printf("result: %s\n", fixedpt_cstr(result, 2));
}

int main() {
  test_muli();
  test_muli2();
  test_divi();
  test_divi2();
  test_mul();
  test_div();
  test_abs();
  test_floor();
  test_ceil();

  return 0;
}
