package core

object CoreConfig {
  val XLEN          = 64

  val GPR_NUM       = 32
  val GPR_LEN       = 5
  val CSR_LEN       = 12

  val START_ADDR    = 0x80000000L
}

object CacheConfig {
  val ADDR_WIDTH    = 64
  val DATA_WIDTH    = 64

  val NSET          = 1 << 8
  val NWAY          = 2
  val BLOCK_SIZE    = 1 << 4
}
