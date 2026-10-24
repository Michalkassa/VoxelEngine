# VoxelEngine

A Minecraft-style voxel engine written from scratch in **Java** with **LWJGL 3** and **OpenGL 3.3**. It generates infinite procedural terrain from Perlin noise, streams chunks in on a background thread, and lets you walk, jump and break blocks in first person.

I built it to learn graphics programming and Java, and eventually to write my own mods for it.

![Procedurally generated valley with stone-capped peaks, running at 300+ FPS](docs/screenshots/terrain.jpg)
<sub>Procedurally generated valley with stone-capped peaks, at 300+ FPS on an Apple Silicon Mac. The debug HUD is in the top left.</sub>

---

## Features

- **Procedural terrain**: several layers of Perlin noise build the height map. Most of the world is small rolling hills and sandy deserts with dunes. Rare mountain ranges rise to snow-capped peaks, with bare stone on cliffs. The same seed always gives the same world.
- **Infinite chunk streaming**: 16×256×16 chunks load and unload around the player on a dedicated `ChunkLoader` thread. Meshes are uploaded to the GPU on the main thread, a few per frame, so the game keeps running smoothly while chunks load.
- **Optimised meshing**: faces hidden by a neighbouring block are skipped, including faces on chunk borders. Each vertex is packed into a single `int`, and blocks are stored in sections so empty air takes no memory.
- **Frustum culling**: chunks outside the camera's view are never drawn.
- **Texture atlas**: every block type samples from a single texture, so only one texture is bound per frame.
- **Game modes**: Survival, with health, and Creative, where you can't take damage and can fly. Press `G` to switch.
- **First-person physics**: AABB collision against the voxel grid, gravity, jumping, acceleration, separate ground and air friction, and flight in Creative.
- **Block interaction**: a ray cast from the camera finds the block you are looking at so you can break it. Holding the button keeps breaking at a limited rate.
- **HUD**: a NanoVG overlay with a crosshair, FPS counter, position, velocity, current chunk, game mode and health.
- **Tests and CI**: JUnit 5 unit tests cover the maths, terrain generation, chunks, physics, player and save files. GitHub Actions builds and tests every pull request.

## Development progress

How the engine got from nothing to where it is now. There's more to come.

| 1. First triangle | 2. Blocks & chunks |
|---|---|
| ![First triangle](docs/screenshots/progress-1.jpg) | ![Random block terrain](docs/screenshots/progress-2.jpg) |
| Getting OpenGL running: shaders, a VAO and vertex colours. | A textured, chunked block world with random heights. |
| **3. Perlin noise terrain** | **4. Today** |
| ![Perlin noise hills](docs/screenshots/progress-3.jpg) | ![Current engine](docs/screenshots/progress-4.jpg) |
| Smooth rolling hills from layered Perlin noise. | Mountains, stone layers, a sky, physics, a HUD, and multithreaded chunk streaming at 300+ FPS. |

### 5. Memory and rendering optimisations

Large render distances were using several gigabytes of memory, so I measured where it was going and rewrote how chunks are stored and drawn. At a render distance of 32 chunks (4,225 chunks loaded):

| | Before | After |
|---|---|---|
| RAM: leftover mesh build data | ~1.5 GB | 0 |
| RAM: block storage | ~833 MB | ~82 MB |
| VRAM: vertex data | ~306 MB | ~27 MB |
| Faces drawn per chunk | ~600 | ~350 |

- **Mesh building:** vertices are built in a reusable `int[]` and thrown away after upload, instead of an `ArrayList<Float>` that each chunk kept forever.
- **Block storage:** blocks are stored in 16×16×16 sections, and sections that are all air aren't allocated.
- **Packed vertices:** each vertex is packed into a single 4-byte `int`, 4 vertices per face. All chunks share one index buffer, and the vertex shader unpacks the position and texture coordinate.
- **Faster meshing:** neighbouring blocks are read directly from the chunk's data, texture coordinates are cached per block type, and faces under the world floor are skipped.
- **No rebuilds on load:** a chunk is meshed once, when all 4 of its neighbours are loaded. Breaking a block only rebuilds a neighbour when the block is on the shared edge.
- **Drawing:** empty chunks are skipped, culling uses each chunk's real height, and the nearest chunks are drawn first.

**What I learned**

- **Measure before optimising.** I expected the GPU to be the problem, but the biggest cost was a list of vertices that every chunk kept after it had already been uploaded.
- **Boxed types are expensive.** An `ArrayList<Float>` stores every number as a separate object: about 20 bytes per float instead of 4.
- **Nested arrays have a hidden cost.** A `byte[16][256][16]` is 4,113 separate Java objects, each with its own header.
- **Most of a chunk is air.** Terrain rarely goes above y ≈ 80 out of 256, so most of the storage is wasted unless empty sections are skipped.
- **Pack data for the GPU.** A face went from 6 vertices × 5 floats (120 bytes) to 4 vertices × 1 int (16 bytes). The shader does the unpacking for free.
- **Don't draw what can never be seen.** Faces on the bottom of the world were about 40% of every chunk's mesh.
- **Order the work so it only happens once.** Waiting until a chunk's neighbours exist means it's meshed once, instead of being rebuilt every time a neighbour loads.

**Next:** placing blocks, saving worlds, biomes, caves and lighting. See the [roadmap](#roadmap).

## Tech stack

| | |
|---|---|
| Language | Java |
| Graphics | OpenGL 3.3 Core via [LWJGL 3.4](https://www.lwjgl.org/) (GLFW, STB, NanoVG) |
| Maths | [JOML](https://github.com/JOML-CI/JOML) |
| Build | Gradle (wrapper included) |
| Testing | JUnit 5, GitHub Actions |

## Getting started

### Requirements

- **macOS on Apple Silicon** (the build currently pulls `natives-macos-arm64`, and the HUD loads the system Helvetica font)
- **JDK 17 or newer** (tested on JDK 25)

You don't need to install Gradle. The `./gradlew` wrapper downloads it for you.

### Run

```bash
git clone https://github.com/Michalkassa/VoxelEngine.git
cd VoxelEngine
./gradlew run
```

The first launch downloads the dependencies. After that, the spawn area is generated before the window starts responding, which takes a few seconds.

> **Running from an IDE (IntelliJ):** set the main class to `Main`, set the working directory to the project root, and add the VM option `-XstartOnFirstThread`. macOS requires GLFW to run on the main thread, and `./gradlew run` adds this option for you automatically.

### Tests

```bash
./gradlew test
```

### Controls

| Key | Action |
|---|---|
| Mouse | Look around |
| `W` `A` `S` `D` | Move |
| `Space` | Jump |
| Double-tap `Space` | Start or stop flying (Creative) |
| `Space` / `Left Shift` | Fly up / down (while flying) |
| Left click | Break the block under the crosshair (hold to keep breaking) |
| `G` | Switch between Survival and Creative |
| `⌘ Q` | Quit |

## How it works

1. **Generation:** `TerrainGenerator` combines several Perlin noise layers for each column: broad swells, small hills, a desert mask with ridged dunes, and a rare mountain mask with ridged peaks. The coordinates are warped so nothing lines up on a grid. Surface blocks are then chosen from height and slope: snow above the snow line, stone on high ground and cliffs, sand in deserts and grass elsewhere.
2. **Streaming:** `ChunkLoader` runs on its own thread. It works out which chunks fall inside the render distance, generates their block data, and queues them.
3. **Meshing:** each frame, `ChunkManager.buildQueuedMeshes()` meshes up to 2 chunks whose 4 neighbours are all loaded, so each chunk is meshed only once. Only visible faces are emitted, and each vertex is packed into a single `int`.
4. **Rendering:** `Renderer` updates the view frustum. Chunks outside it, and chunks with nothing to draw, are skipped. The rest are drawn nearest first, using one shader, one texture atlas and one shared index buffer. The NanoVG HUD is drawn on top.

## Roadmap

- [ ] Block placing and a hotbar
- [ ] Saving and loading worlds
- [ ] Biomes, trees and caves
- [ ] Lighting and ambient occlusion
- [ ] Windows and Linux support
- [ ] Modding API

## Credits

The Perlin noise implementation is adapted from [PavlosMak/PerlinNoise](https://github.com/PavlosMak/PerlinNoise).
