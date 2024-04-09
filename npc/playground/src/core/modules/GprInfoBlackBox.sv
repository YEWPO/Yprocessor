module GprInfoBlackBox (
  input [64*32-1:0] inbits
);

  logic [63:0] gprs[31:0];

  import "DPI-C" function void output_gprs(
    input logic [63:0] gprs[]
  );

  always @(*) begin
    for (int i = 0; i < 32; i++) begin
      gprs[i] = inbits[i*64 +: 64];
    end
    output_gprs(gprs);
  end

endmodule
