package kr.co.opencraft.world;

import kr.co.voxelite.world.BlockRenderLayer;

/**
 * OpenCraft's built-in block definitions.
 */
public final class OpenCraftBlockRegistry {
    private static final BlockRegistry BLOCKS = BlockRegistry.builder()
        .register(BlockDefinition.builder(BlockTypes.AIR, "air")
            .displayName("Air")
            .material(BlockMaterial.AIR)
            .textures(BlockFaceTextures.same(0))
            .solid(false)
            .renderLayer(BlockRenderLayer.SOLID)
            .hardness(0f)
            .lightOpacity(0)
            .build())
        .register(BlockDefinition.builder(BlockTypes.MY_STONE, "my_stone")
            .displayName("Stone")
            .material(BlockMaterial.STONE)
            .textures(BlockFaceTextures.same(0))
            .hardness(1.5f)
            .build())
        .register(BlockDefinition.builder(BlockTypes.ORIGIN_STONE, "origin_stone")
            .displayName("Stone")
            .material(BlockMaterial.STONE)
            .textures(BlockFaceTextures.same(1))
            .hardness(1.5f)
            .build())
        .register(BlockDefinition.builder(BlockTypes.DIRT, "dirt")
            .displayName("Dirt")
            .material(BlockMaterial.EARTH)
            .textures(BlockFaceTextures.same(2))
            .hardness(0.5f)
            .build())
        .register(BlockDefinition.builder(BlockTypes.GRASS, "grass")
            .displayName("Grass")
            .material(BlockMaterial.GRASS)
            .textures(BlockFaceTextures.topSidesBottom(3, 5, 2))
            .hardness(0.6f)
            .build())
        .register(BlockDefinition.builder(BlockTypes.WATER, "water")
            .displayName("Water")
            .material(BlockMaterial.WATER)
            .textures(BlockFaceTextures.same(6))
            .solid(false)
            .renderLayer(BlockRenderLayer.TRANSLUCENT)
            .hardness(100f)
            .lightOpacity(3)
            .build())
        .register(BlockDefinition.builder(BlockTypes.BEDROCK, "bedrock")
            .displayName("Bedrock")
            .material(BlockMaterial.STONE)
            .textures(BlockFaceTextures.same(17))
            .hardness(1000f)
            .build())
        .build();

    public static BlockRegistry blocks() {
        return BLOCKS;
    }

    private OpenCraftBlockRegistry() {
    }
}
