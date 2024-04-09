CVSRCS = init.cpp \
				 exec.cpp \
				 dpi/mem.cpp \
				 dpi/inst.cpp \
				 dpi/gprs.cpp

CVSRCS := $(addprefix $(NSIM_HOME)/src/verilator/, $(CVSRCS))

ifdef CONFIG_WTRACE
CVCFLAGS = -DCONFIG_WTRACE
else
CVCFLAGS =
endif
