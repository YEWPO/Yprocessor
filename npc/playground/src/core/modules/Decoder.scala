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
    val inst = Input(UInt(32.W))
  })

  val rs1 = io.inst(19, 15)
  val rs2 = io.inst(24, 20)
  val rd =  io.inst(11, 7)

  /**                                      type,  rs1,      rs2,    rd,     src1,     src2,     alu,    bu,     lsu */
  val decodeResult = ListLookup(io.inst,
  /** default   -> */                 List(0.U,   0.U,      0.U,    0.U,    SRC1,     SRC2,     0.U,    0.U,    0.U),
    Array(
      lui       ->                    List(U,     0.U,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    0.U),
      auipc     ->                    List(U,     0.U,      0.U,    rd,     PC,       IMM,      ADD,    0.U,    0.U),

      addi      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    0.U),
      slli      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLL,    0.U,    0.U),
      slti      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLT,    0.U,    0.U),
      sltiu     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLTU,   0.U,    0.U),
      xori      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      XOR,    0.U,    0.U),
      srli      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRL,    0.U,    0.U),
      srai      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRA,    0.U,    0.U),
      ori       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      OR,     0.U,    0.U),
      andi      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      AND,    0.U,    0.U),

      add       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     ADD,    0.U,    0.U),
      sub       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SUB,    0.U,    0.U),
      sll       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLL,    0.U,    0.U),
      slt       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLT,    0.U,    0.U),
      sltu      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLTU,   0.U,    0.U),
      xor       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     XOR,    0.U,    0.U),
      srl       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRL,    0.U,    0.U),
      sra       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRA,    0.U,    0.U),
      or        ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     OR,     0.U,    0.U),
      and       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     AND,    0.U,    0.U),

      addiw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADDW,   0.U,    0.U),
      slliw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SLLW,   0.U,    0.U),
      srliw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRLW,   0.U,    0.U),
      sraiw     ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      SRAW,   0.U,    0.U),

      addw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     ADDW,   0.U,    0.U),
      subw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SUBW,   0.U,    0.U),
      sllw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLLW,   0.U,    0.U),
      srlw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SLLW,   0.U,    0.U),
      sraw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     SRAW,   0.U,    0.U),

      jal       ->                    List(J,     0.U,      0.U,    rd,     PC,       IMM,      ADD,    JAL,    0.U),
      jalr      ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    JALR,   0.U),

      beq       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BEQ,    0.U),
      bne       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BNE,    0.U),
      blt       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BLT,    0.U),
      bge       ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BGE,    0.U),
      bltu      ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BLTU,   0.U),
      bgeu      ->                    List(B,     rs1,      rs2,    0.U,    PC,       IMM,      ADD,    BGEU,   0.U),

      lb        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LB ),
      lh        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LH ),
      lw        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LW ),
      ld        ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LD ),
      lbu       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LBU),
      lhu       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LHU),
      lwu       ->                    List(I,     rs1,      0.U,    rd,     SRC1,     IMM,      ADD,    0.U,    LWU),

      sb        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SB ),
      sh        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SH ),
      sw        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SW ),
      sd        ->                    List(S,     rs1,      rs2,    0.U,    SRC1,     IMM,      ADD,    0.U,    SD ),

      mul       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MUL,    0.U,    0.U),
      mulh      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULH,   0.U,    0.U),
      mulhsu    ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULHSU, 0.U,    0.U),
      mulhu     ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULHU,  0.U,    0.U),
      mulw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     MULW,   0.U,    0.U),

      div       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIV,    0.U,    0.U),
      divu      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIVU,   0.U,    0.U),
      rem       ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REM,    0.U,    0.U),
      remu      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REMU,   0.U,    0.U),

      divw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIVW,   0.U,    0.U),
      divuw     ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     DIVUW,  0.U,    0.U),
      remw      ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REMW,   0.U,    0.U),
      remuw     ->                    List(R,     rs1,      rs2,    rd,     SRC1,     SRC2,     REMUW,  0.U,    0.U),

      ebreak    ->                    List(0.U,   0.U,      0.U,    0.U,    SRC1,     SRC2,     0.U,    0.U,    0.U)
    )
  )
}
