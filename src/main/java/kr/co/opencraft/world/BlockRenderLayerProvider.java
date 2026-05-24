package kr.co.opencraft.world;

import java.util.Objects;
import kr.co.voxelite.world.BlockManager;
import kr.co.voxelite.world.BlockRenderLayer;

/**
 * Adapter that connects OpenCraft block definitions to Voxelient render passes.
 */
public class BlockRenderLayerProvider implements BlockManager.IBlockRenderLayerProvider {
    private final BlockRegistry blocks;

    public BlockRenderLayerProvider() {
        this(OpenCraftBlockRegistry.blocks());
    }

    public BlockRenderLayerProvider(BlockRegistry blocks) {
        this.blocks = Objects.requireNonNull(blocks, "blocks");
    }

    @Override
    public BlockRenderLayer getRenderLayer(int blockType) {
        return blocks.getRenderLayer(blockType);
    }
}
