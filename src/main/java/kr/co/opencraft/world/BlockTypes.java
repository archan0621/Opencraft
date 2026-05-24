package kr.co.opencraft.world;

/**
 * Stable block IDs used in saved chunks and network packets.
 *
 * Behavior and texture metadata live in {@link OpenCraftBlockRegistry}.
 */
public class BlockTypes {
    public static final int AIR = -1;

    public static final int MY_STONE = 0;
    public static final int ORIGIN_STONE = 1;
    public static final int DIRT = 2;
    public static final int GRASS = 4;
    public static final int WATER = 6;
    public static final int BEDROCK = 17;

    private BlockTypes() {
    }
}
