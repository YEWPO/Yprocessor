CVSRCS = init.cpp \
				 dpi/mem.cpp \
				 dpi/gprs.cpp

CVSRCS := $(addprefix $(NSIM_HOME)/src/verilator/, $(CVSRCS))
