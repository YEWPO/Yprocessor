package core.modules

import chisel3._
import chisel3.util.BitPat
import chisel3.util.ListLookup
import InstList._
import core.stages.ExuSrc1._
import core.stages.ExuSrc2._
import core.modules.InstType._
import core.modules.AluOp._
import core.modules.BuOp._
import core.stages.LsuOp._

object InstList {
  val lui       = BitPat("b???????_?????_?????_???_?????_01101_11")
  val auipc     = BitPat("b???????_?????_?????_???_?????_00101_11")

  val addi      = BitPat("b???????_?????_?????_000_?????_00100_11")
  val slli      = BitPat("b000000?_?????_?????_001_?????_00100_11")
  val slti      = BitPat("b???????_?????_?????_010_?????_00100_11")
  val sltiu     = BitPat("b???????_?????_?????_011_?????_00100_11")
  val xori      = BitPat("b???????_?????_?????_100_?????_00100_11")
  val srli      = BitPat("b000000?_?????_?????_101_?????_00100_11")
  val srai      = BitPat("b010000?_?????_?????_101_?????_00100_11")
  val ori       = BitPat("b???????_?????_?????_110_?????_00100_11")
  val andi      = BitPat("b???????_?????_?????_111_?????_00100_11")

  val add       = BitPat("b0000000_?????_?????_000_?????_01100_11")
  val sub       = BitPat("b0100000_?????_?????_000_?????_01100_11")
  val sll       = BitPat("b0000000_?????_?????_001_?????_01100_11")
  val slt       = BitPat("b0000000_?????_?????_010_?????_01100_11")
  val sltu      = BitPat("b0000000_?????_?????_011_?????_01100_11")
  val xor       = BitPat("b0000000_?????_?????_100_?????_01100_11")
  val srl       = BitPat("b0000000_?????_?????_101_?????_01100_11")
  val sra       = BitPat("b0100000_?????_?????_101_?????_01100_11")
  val or        = BitPat("b0000000_?????_?????_110_?????_01100_11")
  val and       = BitPat("b0000000_?????_?????_111_?????_01100_11")

  val addiw     = BitPat("b???????_?????_?????_000_?????_00110_11")
  val slliw     = BitPat("b0000000_?????_?????_001_?????_00110_11")
  val srliw     = BitPat("b0000000_?????_?????_101_?????_00110_11")
  val sraiw     = BitPat("b0100000_?????_?????_101_?????_00110_11")

  val addw      = BitPat("b0000000_?????_?????_000_?????_01110_11")
  val subw      = BitPat("b0100000_?????_?????_000_?????_01110_11")
  val sllw      = BitPat("b0000000_?????_?????_001_?????_01110_11")
  val srlw      = BitPat("b0000000_?????_?????_101_?????_01110_11")
  val sraw      = BitPat("b0100000_?????_?????_101_?????_01110_11")

  val jal       = BitPat("b???????_?????_?????_???_?????_11011_11")
  val jalr      = BitPat("b???????_?????_?????_000_?????_11001_11")

  val beq       = BitPat("b???????_?????_?????_000_?????_11000_11")
  val bne       = BitPat("b???????_?????_?????_001_?????_11000_11")
  val blt       = BitPat("b???????_?????_?????_100_?????_11000_11")
  val bge       = BitPat("b???????_?????_?????_101_?????_11000_11")
  val bltu      = BitPat("b???????_?????_?????_110_?????_11000_11")
  val bgeu      = BitPat("b???????_?????_?????_111_?????_11000_11")

  val lb        = BitPat("b???????_?????_?????_000_?????_00000_11")
  val lh        = BitPat("b???????_?????_?????_001_?????_00000_11")
  val lw        = BitPat("b???????_?????_?????_010_?????_00000_11")
  val ld        = BitPat("b???????_?????_?????_011_?????_00000_11")
  val lbu       = BitPat("b???????_?????_?????_100_?????_00000_11")
  val lhu       = BitPat("b???????_?????_?????_101_?????_00000_11")
  val lwu       = BitPat("b???????_?????_?????_110_?????_00000_11")

  val sb        = BitPat("b???????_?????_?????_000_?????_01000_11")
  val sh        = BitPat("b???????_?????_?????_001_?????_01000_11")
  val sw        = BitPat("b???????_?????_?????_010_?????_01000_11")
  val sd        = BitPat("b???????_?????_?????_011_?????_01000_11")

  val mul       = BitPat("b0000001_?????_?????_000_?????_01100_11")
  val mulh      = BitPat("b0000001_?????_?????_001_?????_01100_11")
  val mulhsu    = BitPat("b0000001_?????_?????_010_?????_01100_11")
  val mulhu     = BitPat("b0000001_?????_?????_011_?????_01100_11")
  val mulw      = BitPat("b0000001_?????_?????_000_?????_01110_11")

  val div       = BitPat("b0000001_?????_?????_100_?????_01100_11")
  val divu      = BitPat("b0000001_?????_?????_101_?????_01100_11")
  val rem       = BitPat("b0000001_?????_?????_110_?????_01100_11")
  val remu      = BitPat("b0000001_?????_?????_111_?????_01100_11")

  val divw      = BitPat("b0000001_?????_?????_100_?????_01110_11")
  val divuw     = BitPat("b0000001_?????_?????_101_?????_01110_11")
  val remw      = BitPat("b0000001_?????_?????_110_?????_01110_11")
  val remuw     = BitPat("b0000001_?????_?????_111_?????_01110_11")

  val ebreak    = BitPat("b0000000_00001_00000_000_00000_11100_11")
}

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst        = Input(UInt(32.W))

    val instType    = Output(UInt(typeWidth.W))
    val rs1         = Output(UInt(5.W))
    val rs2         = Output(UInt(5.W))
    val rd          = Output(UInt(5.W))
    val src1Sel     = Output(Bool())
    val src2Sel     = Output(Bool())
    val aluOp       = Output(UInt(aluOpLen.W))
    val buOp        = Output(UInt(buOpLen.W))
    val lsuOp       = Output(UInt(lsuOpLen.W))
    val kill        = Output(Bool())
    val invalid     = Output(Bool())
  })

  val Y = true.B
  val N = false.B

  val rs1 = io.inst(19, 15)
  val rs2 = io.inst(24, 20)
  val rd =  io.inst(11, 7)

  /**                                      type,  rs1,      rs2,    rd,     src1,     src2,     alu,    bu,     lsu,    kill,     invalid */
  val decodeResult = ListLookup(io.inst,
  /** default   -> */                 List(0.U,   0.U,      0.U,    0.U,    SRC1,     SRC2,     0.U,    0.U,    0.U,    N,        Y),
    Array(
      lui       ->                    List(U,     0.U,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    0.U,    N,        N),
      auipc     ->                    List(U,     0.U,      0.U,    rd,     PC,       IMM,      ADD,    0.U,    0.U,    N,        N),

      addi      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    0.U,    N,        N),
      slli      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLL,    0.U,    0.U,    N,        N),
      slti      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLT,    0.U,    0.U,    N,        N),
      sltiu     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLTU,   0.U,    0.U,    N,        N),
      xori      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      XOR,    0.U,    0.U,    N,        N),
      srli      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRL,    0.U,    0.U,    N,        N),
      srai      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRA,    0.U,    0.U,    N,        N),
      ori       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      OR,     0.U,    0.U,    N,        N),
      andi      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      AND,    0.U,    0.U,    N,        N),

      add       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     ADD,    0.U,    0.U,    N,        N),
      sub       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SUB,    0.U,    0.U,    N,        N),
      sll       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLL,    0.U,    0.U,    N,        N),
      slt       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLT,    0.U,    0.U,    N,        N),
      sltu      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLTU,   0.U,    0.U,    N,        N),
      xor       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     XOR,    0.U,    0.U,    N,        N),
      srl       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRL,    0.U,    0.U,    N,        N),
      sra       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRA,    0.U,    0.U,    N,        N),
      or        ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     OR,     0.U,    0.U,    N,        N),
      and       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     AND,    0.U,    0.U,    N,        N),

      addiw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADDW,   0.U,    0.U,    N,        N),
      slliw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLLW,   0.U,    0.U,    N,        N),
      srliw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRLW,   0.U,    0.U,    N,        N),
      sraiw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRAW,   0.U,    0.U,    N,        N),

      addw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     ADDW,   0.U,    0.U,    N,        N),
      subw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SUBW,   0.U,    0.U,    N,        N),
      sllw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLLW,   0.U,    0.U,    N,        N),
      srlw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRLW,   0.U,    0.U,    N,        N),
      sraw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRAW,   0.U,    0.U,    N,        N),

      jal       ->                    List(J,     0.U,      0.U,    rd,     PC,       IMM,      ADD,    JAL,    0.U,    N,        N),
      jalr      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    JALR,   0.U,    N,        N),

      beq       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BEQ,    0.U,    N,        N),
      bne       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BNE,    0.U,    N,        N),
      blt       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BLT,    0.U,    N,        N),
      bge       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BGE,    0.U,    N,        N),
      bltu      ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BLTU,   0.U,    N,        N),
      bgeu      ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BGEU,   0.U,    N,        N),

      lb        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LB,     N,        N),
      lh        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LH,     N,        N),
      lw        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LW,     N,        N),
      ld        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LD,     N,        N),
      lbu       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LBU,    N,        N),
      lhu       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LHU,    N,        N),
      lwu       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LWU,    N,        N),

      sb        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SB,     N,        N),
      sh        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SH,     N,        N),
      sw        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SW,     N,        N),
      sd        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SD,     N,        N),

      mul       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MUL,    0.U,    0.U,    N,        N),
      mulh      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULH,   0.U,    0.U,    N,        N),
      mulhsu    ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULHSU, 0.U,    0.U,    N,        N),
      mulhu     ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULHU,  0.U,    0.U,    N,        N),
      mulw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULW,   0.U,    0.U,    N,        N),

      div       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIV,    0.U,    0.U,    N,        N),
      divu      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIVU,   0.U,    0.U,    N,        N),
      rem       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REM,    0.U,    0.U,    N,        N),
      remu      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REMU,   0.U,    0.U,    N,        N),

      divw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIVW,   0.U,    0.U,    N,        N),
      divuw     ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIVUW,  0.U,    0.U,    N,        N),
      remw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REMW,   0.U,    0.U,    N,        N),
      remuw     ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REMUW,  0.U,    0.U,    N,        N),

      ebreak    ->                    List(0.U,   0.U,      0.U,    0.U,    SRC1,     SRC2,     0.U,    0.U,    0.U,    Y,        N)
    )
  )

  io.instType     := decodeResult(0)
  io.rs1          := decodeResult(1)
  io.rs2          := decodeResult(2)
  io.rd           := decodeResult(3)
  io.src1Sel      := decodeResult(4)
  io.src2Sel      := decodeResult(5)
  io.aluOp        := decodeResult(6)
  io.buOp         := decodeResult(7)
  io.lsuOp        := decodeResult(8)
  io.kill         := decodeResult(9)
  io.invalid      := decodeResult(10)
}
