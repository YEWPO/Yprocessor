CVSRCS = init.cpp \
				 dpi/mem.cpp

CVSRCS := $(addprefix $(NSIM_HOME)/src/verilator/, $(CVSRCS))
