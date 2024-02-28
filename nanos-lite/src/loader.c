#include <proc.h>
#include <elf.h>

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

#if defined(__ISA_AM_NATIVE__)
#define EXPECT_TYPE EM_X86_64
#elif defined(__ISA_X86__)
#define EXPECT_TYPE EM_X86_64
#elif defined(__ISA_MIPS__)
#define EXPECT_TYPE EM_MIPS
#elif defined(__ISA_RISCV64__) || defined(__ISA_RISCV32__)
#define EXPECT_TYPE EM_RISCV
#endif

size_t ramdisk_read(void *buf, size_t offset, size_t len);
size_t ramdisk_write(const void *buf, size_t offset, size_t len);
size_t get_ramdisk_size();

Elf_Ehdr elf_ehdr;
Elf_Phdr elf_phdr;

static uintptr_t loader(PCB *pcb, const char *filename) {
  ramdisk_read(&elf_ehdr, 0, sizeof(Elf_Ehdr));
  assert(*(uint32_t *)elf_ehdr.e_ident == 0x464c457f);
  assert(elf_ehdr.e_type == ET_EXEC);
  assert(elf_ehdr.e_machine == EXPECT_TYPE);

  for (int i = 0; i < elf_ehdr.e_phnum; ++i) {
    ramdisk_read(&elf_phdr, elf_ehdr.e_phoff + i * sizeof(Elf_Phdr), sizeof(Elf_Phdr));
    if (elf_phdr.p_type == PT_LOAD) {
      ramdisk_read((void *)elf_phdr.p_vaddr, elf_phdr.p_offset, elf_phdr.p_filesz);
      memset((void *)(elf_phdr.p_vaddr + elf_phdr.p_filesz), 0, elf_phdr.p_memsz - elf_phdr.p_filesz);
    }
  }

  return elf_ehdr.e_entry;
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %p", entry);
  ((void(*)())entry) ();
}

