CVSRCS = init.cpp \
				 exec.cpp \
				 dpi/mem.cpp \
				 dpi/inst.cpp \
				 dpi/gprs.cpp

CVSRCS := $(addprefix $(NSIM_HOME)/src/verilator/, $(CVSRCS))

LIBVTOP = libVTop.a libverilated.a VTop__ALL.a
LIBVTOP := $(addprefix $(NSIM_HOME)/src/verilator/obj_dir/, $(LIBVTOP))

ifdef CONFIG_WTRACE
CVCFLAGS = -DCONFIG_WTRACE
else
CVCFLAGS =
endif

wv:
	gtkwave $(NSIM_HOME)/build/trace.vcd &

.PHONY: wv
