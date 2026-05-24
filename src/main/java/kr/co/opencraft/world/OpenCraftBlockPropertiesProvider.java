package kr.co.opencraft.world;

import java.util.Objects;
import kr.co.voxelite.world.BlockManager;

/**
 * Adapter that connects OpenCraft block definitions to Voxelite physics.
 */
public class OpenCraftBlockPropertiesProvider implements BlockManager.IBlockPropertiesProvider {
    private final BlockRegistry blocks;

    public OpenCraftBlockPropertiesProvider() {
        this(OpenCraftBlockRegistry.blocks());
    }

    public OpenCraftBlockPropertiesProvider(BlockRegistry blocks) {
        this.blocks = Objects.requireNonNull(blocks, "blocks");
    }

    @Override
    public boolean isSolid(int blockType) {
        return blocks.isSolid(blockType);
    }
}
