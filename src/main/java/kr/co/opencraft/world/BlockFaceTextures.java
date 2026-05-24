package kr.co.opencraft.world;

/**
 * Texture indices for the six voxel faces.
 *
 * Face order matches voxelient's mesh builder:
 * front=z+, back=z-, left=x-, right=x+, top=y+, bottom=y-.
 */
public record BlockFaceTextures(
    int front,
    int back,
    int left,
    int right,
    int top,
    int bottom
) {
    public static BlockFaceTextures same(int textureIndex) {
        return new BlockFaceTextures(
            textureIndex,
            textureIndex,
            textureIndex,
            textureIndex,
            textureIndex,
            textureIndex
        );
    }

    public static BlockFaceTextures topSidesBottom(int top, int sides, int bottom) {
        return new BlockFaceTextures(sides, sides, sides, sides, top, bottom);
    }

    public int textureForFace(int faceIndex) {
        return switch (faceIndex) {
            case 0 -> front;
            case 1 -> back;
            case 2 -> left;
            case 3 -> right;
            case 4 -> top;
            case 5 -> bottom;
            default -> 0;
        };
    }

    public int[] toArray() {
        return new int[] { front, back, left, right, top, bottom };
    }
}
