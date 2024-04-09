module InstInfoBlackBox (
  input [31:0] inst,
  input [63:0] dnpc,
  input        kill,
  input        invalid,
  input        en
);

  reg [31:0] inst_reg;
  reg [63:0] dnpc_reg;
  reg        kill_reg;
  reg        invalid_reg;
  reg        en_reg;

  assign inst_reg     = inst;
  assign dnpc_reg     = dnpc;
  assign kill_reg     = kill;
  assign invalid_reg  = invalid;
  assign en_reg       = en;

  import "DPI-C" function void output_inst(
    input int     inst,
    input longint dnpc,
    input logic   kill,
    input logic   invalid,
    input logic   en
  );

  always @(*) begin
    output_inst(inst_reg, dnpc_reg, kill_reg, invalid_reg, en_reg);
  end

endmodule
