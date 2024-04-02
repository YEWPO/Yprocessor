module SramRead(
  input         clk,
  input         en,
  input         addr,
  output [63:0] data
);

  import "DPI-C" function void read_mem(
    input  int addr,
    output longint data
  );

  always@(posedge clk) begin
    if(en) begin
      read_mem(addr, data);
    end
    else begin
      data <= 64'h0;
    end
  end

endmodule
