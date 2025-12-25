package org.example;

public class Block {
    private int x, y, z;
    private BlockType type;

    public Block(int x, int y, int z, BlockType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public BlockType getType() { return type; }

    public enum BlockType {
        // ID считаем слева-направо, сверху-вниз.
        // 0 = трава сверху, 1 = камень, 2 = земля, 3 = трава сбоку

        GRASS(3),    // По умолчанию берем текстуру "сбоку"
        DIRT(2),     // Земля
        STONE(1),    // Камень
        WOOD(4),     // Доски
        LOG(20),     // Бревно (Боковая сторона)
        LEAVES(52);  // Листва (Дуб)

        public final int textureId;

        BlockType(int textureId) {
            this.textureId = textureId;
        }
    }
}