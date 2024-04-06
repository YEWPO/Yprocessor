VERILATOR_ARCHIVES = VTop__ALL.a libverilated.a libVTop.a

VERILATOR_ARCHIVES := $(addprefix $(NSIM_HOME)/src/verilator/obj_dir/,$(VERILATOR_ARCHIVES))
