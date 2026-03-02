package kr.co.opencraft.world;

import java.util.HashMap;
import java.util.Map;

/**
 * Block texture configuration for each face
 * Allows different textures for each of the 6 faces of a block
 */
public class BlockTextures {
    
    /**
     * Texture configuration for a single block
     * Face order: [front, back, left, right, top, bottom]
     * Indices: [0=z+, 1=z-, 2=x-, 3=x+, 4=y+, 5=y-]
     */
    public static class FaceTextures {
        public final int front;   // z+ (0)
        public final int back;    // z- (1)
        public final int left;    // x- (2)
        public final int right;   // x+ (3)
        public final int top;     // y+ (4)
        public final int bottom;  // y- (5)
        
        /**
         * All faces use the same texture
         */
        public FaceTextures(int allFaces) {
            this(allFaces, allFaces, allFaces, allFaces, allFaces, allFaces);
        }
        
        /**
         * Top/Bottom different, sides same
         */
        public FaceTextures(int top, int sides, int bottom) {
            this(sides, sides, sides, sides, top, bottom);
        }
        
        /**
         * Each face has different texture
         */
        public FaceTextures(int front, int back, int left, int right, int top, int bottom) {
            this.front = front;
            this.back = back;
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
        
        /**
         * Get texture index for a specific face
         * @param faceIndex Face index (0=front, 1=back, 2=left, 3=right, 4=top, 5=bottom)
         */
        public int getTexture(int faceIndex) {
            switch (faceIndex) {
                case 0: return front;
                case 1: return back;
                case 2: return left;
                case 3: return right;
                case 4: return top;
                case 5: return bottom;
                default: return 0;
            }
        }
        
        /**
         * Get all textures as array
         */
        public int[] toArray() {
            return new int[] { front, back, left, right, top, bottom };
        }
    }
    
    // Block texture registry
    private static final Map<Integer, FaceTextures> BLOCK_TEXTURES = new HashMap<>();
    
    static {
        // Register default block textures
        registerBlockTextures();
    }
    
    /**
     * Register all block textures
     */
    private static void registerBlockTextures() {
        // MY_STONE (0) - all faces same
        register(BlockTypes.MY_STONE, new FaceTextures(0));
        
        // ORIGIN_STONE (1) - all faces same
        register(BlockTypes.ORIGIN_STONE, new FaceTextures(1));
        
        // DIRT (2) - all faces same
        register(BlockTypes.DIRT, new FaceTextures(2));
        
        // GRASS (4) - top/sides/bottom different
        register(BlockTypes.GRASS, new FaceTextures(
            3,  // top: texture 3 (grass top)
            5,  // sides: texture 5 (grass side)
            2   // bottom: texture 2 (dirt)
        ));
        
        // BEDROCK (17) - all faces same
        register(BlockTypes.BEDROCK, new FaceTextures(17));
        
        // Example: Grass block (top/sides/bottom different)
        // register(BlockTypes.GRASS, new FaceTextures(
        //     0,  // top: grass texture
        //     1,  // sides: dirt with grass
        //     2   // bottom: dirt
        // ));
        
        // Example: Log block (top/bottom different from sides)
        // register(BlockTypes.LOG, new FaceTextures(
        //     4,  // top: wood rings
        //     3,  // sides: bark
        //     4   // bottom: wood rings
        // ));
        
        // Example: Furnace (all 6 faces different)
        // register(BlockTypes.FURNACE, new FaceTextures(
        //     5,  // front: furnace face
        //     6,  // back: stone
        //     6,  // left: stone
        //     6,  // right: stone
        //     7,  // top: stone
        //     7   // bottom: stone
        // ));
    }
    
    /**
     * Register texture configuration for a block type
     */
    public static void register(int blockType, FaceTextures textures) {
        BLOCK_TEXTURES.put(blockType, textures);
    }
    
    /**
     * Get texture configuration for a block type
     * Returns default (all faces = blockType) if not registered
     */
    public static FaceTextures get(int blockType) {
        return BLOCK_TEXTURES.getOrDefault(blockType, new FaceTextures(blockType));
    }
    
    /**
     * Get texture index for a specific face of a block
     */
    public static int getTexture(int blockType, int faceIndex) {
        return get(blockType).getTexture(faceIndex);
    }
    
    /**
     * Check if a block has custom face textures
     */
    public static boolean hasCustomTextures(int blockType) {
        return BLOCK_TEXTURES.containsKey(blockType);
    }
    
    private BlockTextures() {
        // Utility class
    }
}
