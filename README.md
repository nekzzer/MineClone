# MineClone

A simple Minecraft-like voxel game built with Java and LWJGL (OpenGL).

![Java](https://img.shields.io/badge/Java-17+-orange)
![LWJGL](https://img.shields.io/badge/LWJGL-3.x-blue)

## Features

- Procedurally generated terrain using Perlin noise
- Block placing and breaking
- Multiple block types (Grass, Dirt, Stone, Wood)
- Tree generation
- First-person camera with physics (gravity, jumping, collision)
- Texture atlas support
- FPS counter

## Controls

| Key | Action |
|-----|--------|
| W/A/S/D | Move |
| Mouse | Look around |
| Space | Jump |
| Left Click | Break block |
| Right Click | Place block |
| 1-4 | Select block type |
| ESC | Exit |

## Requirements

- Java 17+
- Maven

## Build & Run

```bash
mvn clean compile exec:java
```

## Project Structure

```
src/main/java/org/example/
├── Main.java        - Entry point, window, input handling
├── Camera.java      - Player movement, physics, collision
├── World.java       - Block storage, terrain generation, rendering
├── Block.java       - Block types and properties
├── Texture.java     - Texture loading (STB)
└── PerlinNoise.java - Noise generation for terrain
```

## License

MIT
