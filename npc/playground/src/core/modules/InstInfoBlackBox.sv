module InstInfoBlackBox (
  input         clk,
  input         rst,
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

  import "DPI-C" function void output_inst(
    input int     inst,
    input longint dnpc,
    input logic   kill,
    input logic   invalid,
    input logic   en
  );

  always @(posedge clk) begin
    if (rst) begin
      inst_reg    <= 32'h0;
      dnpc_reg    <= 64'h0;
      kill_reg    <= 1'b0;
      invalid_reg <= 1'b0;
      en_reg      <= 1'b0;
    end else begin
      inst_reg    <= inst;
      dnpc_reg    <= dnpc;
      kill_reg    <= kill;
      invalid_reg <= invalid;
      en_reg      <= en;
    end
  end

  wire nclk;
  assign nclk = ~clk;

  always @(posedge nclk) begin
    output_inst(inst_reg, dnpc_reg, kill_reg, invalid_reg, en_reg);
  end

endmodule
