#include <proc.h>
#include <elf.h>
#include <fs.h>

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

int fs_open(const char *pathname, int flags, int mode);
size_t fs_read(int fd, void *buf, size_t len);
size_t fs_write(int fd, const void *buf, size_t len);
size_t fs_lseek(int fd, size_t offset, int whence);
int fs_close(int fd);

Elf_Ehdr elf_ehdr;
Elf_Phdr elf_phdr;

static uintptr_t loader(PCB *pcb, const char *filename) {
  int loader_fd = fs_open(filename, 0, 0);

  fs_read(loader_fd, &elf_ehdr, sizeof(Elf_Ehdr));
  assert(*(uint32_t *)elf_ehdr.e_ident == 0x464c457f);
  assert(elf_ehdr.e_type == ET_EXEC);
  assert(elf_ehdr.e_machine == EXPECT_TYPE);

  for (int i = 0; i < elf_ehdr.e_phnum; ++i) {
    fs_lseek(loader_fd, elf_ehdr.e_phoff + i * (sizeof(Elf_Phdr)), SEEK_SET);
    fs_read(loader_fd, &elf_phdr, sizeof(Elf_Phdr));
    if (elf_phdr.p_type == PT_LOAD) {
      fs_lseek(loader_fd, elf_phdr.p_offset, SEEK_SET);
      fs_read(loader_fd, (void *)elf_phdr.p_vaddr, elf_phdr.p_filesz);
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

