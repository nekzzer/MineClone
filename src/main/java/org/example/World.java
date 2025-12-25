package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.lwjgl.opengl.GL11.*;

public class World {
    private List<Block> blocks = new ArrayList<>();

    private int worldSize = 30;
    private PerlinNoise noiseGen = new PerlinNoise();
    private Random random = new Random(); 
    private Texture texture; 

    public World() {

        this.texture = new Texture("atlas.png");

        generateWorld();
    }

    private void generateWorld() {
        for (int x = -worldSize; x < worldSize; x++) {
            for (int z = -worldSize; z < worldSize; z++) {

                double frequency = 0.1;
                double amplitude = 8.0;

                double noiseValue = noiseGen.noise(x * frequency, 0, z * frequency);
                int height = (int) (noiseValue * amplitude);

                addBlock(x, height, z, Block.BlockType.GRASS);

                for (int y = height - 1; y > height - 4; y--) {
                    addBlock(x, y, z, Block.BlockType.DIRT);
                }


                if (random.nextFloat() < 0.02 &&
                        x > -worldSize + 2 && x < worldSize - 2 &&
                        z > -worldSize + 2 && z < worldSize - 2) {

                    generateTree(x, height + 1, z);
                }
            }
        }
    }

    private void generateTree(int x, int y, int z) {
        int h = 4 + random.nextInt(2); 

        for (int i = 0; i < h; i++) {
            addBlock(x, y + i, z, Block.BlockType.LOG);
        }

        for (int ly = y + h - 2; ly <= y + h + 1; ly++) {
            for (int lx = x - 2; lx <= x + 2; lx++) {
                for (int lz = z - 2; lz <= z + 2; lz++) {
                    if (lx == x && lz == z && ly < y + h) continue;
                    if (Math.abs(lx - x) == 2 && Math.abs(lz - z) == 2) continue;
                    if (ly == y + h + 1 && (Math.abs(lx - x) > 1 || Math.abs(lz - z) > 1)) continue;

                    addBlock(lx, ly, lz, Block.BlockType.LEAVES);
                }
            }
        }
    }

    public void addBlock(int x, int y, int z, Block.BlockType type) {
        if (getBlockAt(x, y, z) != null) return;
        blocks.add(new Block(x, y, z, type));
    }

    public boolean removeBlock(int x, int y, int z) {
        return blocks.removeIf(block ->
                block.getX() == x && block.getY() == y && block.getZ() == z
        );
    }

    public Block getBlockAt(int x, int y, int z) {
        for (Block block : blocks) {
            if (block.getX() == x && block.getY() == y && block.getZ() == z) {
                return block;
            }
        }
        return null;
    }

    public void render() {
        glEnable(GL_TEXTURE_2D);
        texture.bind();

        glColor3f(1, 1, 1); 

        glBegin(GL_QUADS);
        for (Block block : blocks) {
            renderBlock(block);
        }
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }

    private void renderBlock(Block block) {
        float x = block.getX();
        float y = block.getY();
        float z = block.getZ();
        float size = 0.5f;

        int baseId = block.getType().textureId;
        int sideId = baseId;
        int topId = baseId;
        int bottomId = baseId;

        if (block.getType() == Block.BlockType.GRASS) {
            topId = 0;   
            bottomId = 2;
            sideId = 3; 
        }
        if (block.getType() == Block.BlockType.LOG) {
            topId = 21;
            bottomId = 21;
            sideId = 20;
        }

        glBegin(GL_QUADS);

        tex(sideId, 0, 1); glVertex3f(x - size, y - size, z + size);
        tex(sideId, 1, 1); glVertex3f(x + size, y - size, z + size);
        tex(sideId, 1, 0); glVertex3f(x + size, y + size, z + size);
        tex(sideId, 0, 0); glVertex3f(x - size, y + size, z + size);

        tex(sideId, 1, 1); glVertex3f(x - size, y - size, z - size);
        tex(sideId, 1, 0); glVertex3f(x - size, y + size, z - size);
        tex(sideId, 0, 0); glVertex3f(x + size, y + size, z - size);
        tex(sideId, 0, 1); glVertex3f(x + size, y - size, z - size);


        if (block.getType() == Block.BlockType.GRASS) {
            glColor3f(0.4f, 0.8f, 0.3f); 
        }

        tex(topId, 0, 0); glVertex3f(x - size, y + size, z - size);
        tex(topId, 0, 1); glVertex3f(x - size, y + size, z + size);
        tex(topId, 1, 1); glVertex3f(x + size, y + size, z + size);
        tex(topId, 1, 0); glVertex3f(x + size, y + size, z - size);

        glColor3f(1, 1, 1);

        tex(bottomId, 0, 0); glVertex3f(x - size, y - size, z - size);
        tex(bottomId, 1, 0); glVertex3f(x + size, y - size, z - size);
        tex(bottomId, 1, 1); glVertex3f(x + size, y - size, z + size);
        tex(bottomId, 0, 1); glVertex3f(x - size, y - size, z + size);

        tex(sideId, 1, 1); glVertex3f(x + size, y - size, z - size);
        tex(sideId, 1, 0); glVertex3f(x + size, y + size, z - size);
        tex(sideId, 0, 0); glVertex3f(x + size, y + size, z + size);
        tex(sideId, 0, 1); glVertex3f(x + size, y - size, z + size);

        tex(sideId, 1, 1); glVertex3f(x - size, y - size, z - size);
        tex(sideId, 0, 1); glVertex3f(x - size, y - size, z + size);
        tex(sideId, 0, 0); glVertex3f(x - size, y + size, z + size);
        tex(sideId, 1, 0); glVertex3f(x - size, y + size, z - size);

        glEnd();
    }

    private void tex(int id, float uOff, float vOff) {
        float u = (id % 16) / 16.0f;
        float v = (id / 16) / 16.0f;
        float uStep = 1.0f / 16.0f;
        float vStep = 1.0f / 16.0f;

        glTexCoord2f(u + (uOff * uStep), v + (vOff * vStep));
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}