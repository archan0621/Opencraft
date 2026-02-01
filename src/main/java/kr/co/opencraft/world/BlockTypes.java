package kr.co.opencraft.world;

/**
 * Block type definitions for OpenCraft
 * Maps block IDs to texture atlas positions
 */
public class BlockTypes {
    // Air (no block)
    public static final int AIR = -1;
    
    // Basic blocks (0-15: first row of atlas)
    public static final int MY_STONE = 0;
    public static final int ORIGIN_STONE = 1;
    public static final int DIRT = 2;
    public static final int BEDROCK = 3;

    
    private BlockTypes() {
        // Utility class
    }
}
