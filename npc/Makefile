WORK_DIR = $(abspath .)

BUILD_DIR = $(WORK_DIR)/build

string_in_red = "\033[31;1m"$(1)"\033[0m"

build:
	$(call git_commit, "generate verilog")
	@echo $(call string_in_red, "=> Build Project")
	@mill -i __.runMain Main --target-dir $(BUILD_DIR)

test:
	@echo $(call string_in_red, "=> Test Project")
	@mill -i __.test

TESTCASE ?=

testonly:
	@echo $(call string_in_red, "=> TestOnly "$(TESTCASE))
	@mill -i __.testOnly $(TESTCASE)

clean:
	@echo $(call string_in_red, "=> Clean Project")
	@mill -i clean
	@rm -rf $(BUILD_DIR)

reformat:
	@echo $(call string_in_red, "=> Reformat Project")
	@mill -i __.reformat

CVCFLAGS ?=

verilator: build
	@echo $(call string_in_red, "=> Verilator Project")
	@verilator --cc --build \
		--top-module Top \
		--trace \
		-CFLAGS "$(CVCFLAGS)" \
		-O3 --x-assign fast --x-initial fast --noassert \
		--Mdir $(NSIM_HOME)/src/verilator/obj_dir \
		`find $(BUILD_DIR)/gen -name "*.sv" | tr '\n' ' '` \
		`find $(NSIM_HOME)/src/verilator -name "*.cpp" | grep -Ev 'obj_dir' | tr '\n' ' '`

.PHONY: build clean reformat test testonly verilator

include ../Makefile
