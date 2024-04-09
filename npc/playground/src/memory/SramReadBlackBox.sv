module SramReadBlackBox (
  input         clk,
  input         en,
  input  [63:0] addr,
  output [63:0] data
);

  import "DPI-C" function longint read_mem(
    input longint addr,
    input logic   en
  );

  assign data = en ? read_mem(addr, en) : 64'h0;

endmodule
