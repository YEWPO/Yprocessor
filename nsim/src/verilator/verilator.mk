VERILATOR_ARCHIVES = VTop__ALL.a libverilated.a

VERILATOR_ARCHIVES := $(addprefix $(NSIM_HOME)/src/verilator/obj_dir/,$(VERILATOR_ARCHIVES))
