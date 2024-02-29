#include <NDL.h>
#include <SDL.h>

#define keyname(k) #k,

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

static uint8_t keystate[256];

#define NR_KEY (sizeof(keyname) / sizeof(keyname[0]))

int SDL_PushEvent(SDL_Event *ev) {
  return 0;
}

int SDL_PollEvent(SDL_Event *ev) {
  char kbd_event[64];
  if (!NDL_PollEvent(kbd_event, 64)) return 0;

  char kbd_type[5], kbd_sym[16];

  sscanf(kbd_event, "%s %s\n", kbd_type, kbd_sym);

  if (strcmp(kbd_type, "kd") == 0) {
    ev->key.type = SDL_KEYDOWN;
  } else {
    ev->key.type = SDL_KEYUP;
  }

  for (int i = 0; i < NR_KEY; ++i) {
    if (strcmp(kbd_sym, keyname[i]) == 0) {
      ev->key.keysym.sym = i;
      keystate[i] = ev->key.type == SDL_KEYDOWN ? 1 : 0;
      break;
    }
  }

  return 1;
}

int SDL_WaitEvent(SDL_Event *event) {
  while (!SDL_PollEvent(event));
  return 1;
}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask) {
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys) {
  return keystate;
}
