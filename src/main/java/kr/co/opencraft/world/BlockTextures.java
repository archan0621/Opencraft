package kr.co.opencraft.world;

import java.util.HashMap;
import java.util.Map;

/**
 * Compatibility facade for older texture lookups.
 *
 * New block metadata should be registered in {@link OpenCraftBlockRegistry}.
 */
public final class BlockTextures {
    private static final Map<Integer, BlockFaceTextures> OVERRIDES = new HashMap<>();

    /**
     * Legacy wrapper kept so existing callers do not need to know about the registry.
     */
    public static final class FaceTextures {
        public final int front;
        public final int back;
        public final int left;
        public final int right;
        public final int top;
        public final int bottom;

        public FaceTextures(int allFaces) {
            this(BlockFaceTextures.same(allFaces));
        }

        public FaceTextures(int top, int sides, int bottom) {
            this(BlockFaceTextures.topSidesBottom(top, sides, bottom));
        }

        public FaceTextures(int front, int back, int left, int right, int top, int bottom) {
            this(new BlockFaceTextures(front, back, left, right, top, bottom));
        }

        private FaceTextures(BlockFaceTextures textures) {
            front = textures.front();
            back = textures.back();
            left = textures.left();
            right = textures.right();
            top = textures.top();
            bottom = textures.bottom();
        }

        public int getTexture(int faceIndex) {
            return toBlockFaceTextures().textureForFace(faceIndex);
        }

        public int[] toArray() {
            return toBlockFaceTextures().toArray();
        }

        private BlockFaceTextures toBlockFaceTextures() {
            return new BlockFaceTextures(front, back, left, right, top, bottom);
        }
    }

    /**
     * @deprecated Register built-in blocks in {@link OpenCraftBlockRegistry}.
     */
    @Deprecated
    public static void register(int blockType, FaceTextures textures) {
        OVERRIDES.put(blockType, textures.toBlockFaceTextures());
    }

    public static FaceTextures get(int blockType) {
        BlockFaceTextures override = OVERRIDES.get(blockType);
        if (override != null) {
            return new FaceTextures(override);
        }
        return new FaceTextures(OpenCraftBlockRegistry.blocks().get(blockType).textures());
    }

    public static int getTexture(int blockType, int faceIndex) {
        return get(blockType).getTexture(faceIndex);
    }

    public static boolean hasCustomTextures(int blockType) {
        return OVERRIDES.containsKey(blockType) || OpenCraftBlockRegistry.blocks().isKnown(blockType);
    }

    private BlockTextures() {
    }
}
