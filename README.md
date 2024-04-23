# Yprocessor

> The fifth **One Student One Chip** project.
>
> Previous implement of my processor referred as **YPC** is available at [here](https://github.com/YEWPO/YPC).  Because v2.2 has been released in YPC, so I decided to start to release v3.0 in Yprocessor.

**Yprocessor** is a processor project, based on RISC-V.

In order to implement the processor, the project framework implements the RISC-V simulator, referred as **NEMU**.  You can find NEMU in `nemu` directory.

In addition, the **abstract machine**, **Nanos-lite** operating system and **Navy Applications** are also implemented, which can be found in `abstract-machine`, `nanos-lite`, `navy-apps` directories respectively.

The most important thing is the RTL implementation of the processor core, which you can find in the `npc` directory. What's more , the **NPC** is implemented by Chisel.

After RTL coding, we need to simulate our RTL design and verify the correctness, **NSIM** is implemented here, which you can find in `nsim` directory. NSIM is based on NEMU framework, with less work, you can utilize all the functions you have achieved in NEMU in NSIM. This [blog](https://yewpo.top/153/) describes the design details of NSIM.
