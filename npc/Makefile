WORK_DIR = $(abspath .)

BUILD_DIR = $(WORK_DIR)/build

sting_in_red = "\033[31;1m"$(1)"\033[0m"

build:
	$(call git_commit, "generate verilog")
	@echo $(call sting_in_red, "=> Build Project")
	@mill -i __.runMain Main --target-dir $(BUILD_DIR)

test:
	@echo $(call sting_in_red, "=> Test Project")
	@mill -i __.test

clean:
	@echo $(call sting_in_red, "=> Clean Project")
	@mill -i clean
	@rm -rf $(BUILD_DIR)

reformat:
	@echo $(call sting_in_red, "=> Reformat Project")
	@mill -i __.reformat

.PHONY: build clean reformat test

include ../Makefile
