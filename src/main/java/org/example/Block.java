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


        GRASS(3),    
        DIRT(2),    
        STONE(1),    
        WOOD(4),     
        LOG(20),     
        LEAVES(52); 

        public final int textureId;

        BlockType(int textureId) {
            this.textureId = textureId;
        }
    }
}
