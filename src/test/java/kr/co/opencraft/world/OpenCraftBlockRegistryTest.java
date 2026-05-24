package kr.co.opencraft.world;

import kr.co.voxelite.world.BlockRenderLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenCraftBlockRegistryTest {
    private final BlockRegistry blocks = OpenCraftBlockRegistry.blocks();

    @Test
    void water_ShouldCarrySharedPhysicsAndRenderMetadata() {
        BlockDefinition water = blocks.get(BlockTypes.WATER);

        assertFalse(water.solid());
        assertEquals(BlockMaterial.WATER, water.material());
        assertEquals(BlockRenderLayer.TRANSLUCENT, water.renderLayer());
        assertEquals(6, water.textureForFace(0));
    }

    @Test
    void grass_ShouldExposePerFaceTexturesFromRegistry() {
        assertArrayEquals(
            new int[] { 5, 5, 5, 5, 3, 2 },
            blocks.get(BlockTypes.GRASS).textures().toArray()
        );
    }

    @Test
    void unknownPositiveBlocks_ShouldKeepLegacySolidBehavior() {
        BlockDefinition unknown = blocks.get(42);

        assertTrue(unknown.solid());
        assertEquals(42, unknown.textureForFace(4));
        assertFalse(blocks.isKnown(42));
    }

    @Test
    void providers_ShouldDelegateToTheSameRegistry() {
        OpenCraftBlockPropertiesProvider propertiesProvider = new OpenCraftBlockPropertiesProvider(blocks);
        BlockTextureProvider textureProvider = new BlockTextureProvider(blocks);
        BlockRenderLayerProvider renderLayerProvider = new BlockRenderLayerProvider(blocks);

        assertFalse(propertiesProvider.isSolid(BlockTypes.WATER));
        assertTrue(propertiesProvider.isSolid(BlockTypes.ORIGIN_STONE));
        assertEquals(3, textureProvider.getTexture(BlockTypes.GRASS, 4));
        assertEquals(BlockRenderLayer.TRANSLUCENT, renderLayerProvider.getRenderLayer(BlockTypes.WATER));
    }
}
