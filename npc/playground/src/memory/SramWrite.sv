module SramWrite (
  input         clk,
  input         en,
  input [63:0]  addr,
  input [63:0]  data,
  input [7:0]   strb
);

  import "DPI-C" function void write_mem(
    input int   addr,
    input int   data,
    input int   strb
  );

  wire [7:0] strb_1;

  strb_1 = en ? strb : 8'h0;

  always @(posedge clk) begin
    write_mem(addr, data, strb_1);
  end

endmodule
