package kr.co.opencraft.world;

/**
 * 간소화한 Minecraft-style biome profile.
 * baseHeight / heightVariation은 오버월드 밀도장 계산에 직접 사용된다.
 */
public enum Biome {
    PLAINS(0.125F, 0.08F, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.MY_STONE),
    FOREST(0.20F, 0.18F, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.MY_STONE),
    HILLS(0.65F, 0.33F, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.MY_STONE),
    MOUNTAIN(1.15F, 0.65F, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.ORIGIN_STONE);

    public final float baseHeight;
    public final float heightVariation;
    public final int surfaceBlock;
    public final int subsurfaceBlock;
    public final int stoneBlock;

    Biome(float baseHeight, float heightVariation, int surfaceBlock, int subsurfaceBlock, int stoneBlock) {
        this.baseHeight = baseHeight;
        this.heightVariation = heightVariation;
        this.surfaceBlock = surfaceBlock;
        this.subsurfaceBlock = subsurfaceBlock;
        this.stoneBlock = stoneBlock;
    }
}
