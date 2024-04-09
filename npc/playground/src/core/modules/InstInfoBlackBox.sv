module InstInfoBlackBox (
  input [31:0] inst,
  input [63:0] dnpc,
  input        valid
);

  reg [31:0] inst_reg;
  reg [63:0] dnpc_reg;
  reg        valid_reg;

  assign inst_reg = inst;
  assign dnpc_reg = dnpc;
  assign valid_reg = valid;

  import "DPI-C" function void output_inst(
    input int     inst,
    input longint dnpc,
    input logic   valid
  );

  always @(*) begin
    output_inst(inst_reg, dnpc_reg, valid_reg);
  end

endmodule
