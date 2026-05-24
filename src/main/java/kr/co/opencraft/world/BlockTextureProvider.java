package kr.co.opencraft.world;

import java.util.Objects;
import kr.co.voxelite.world.BlockManager;

/**
 * Adapter that connects OpenCraft block definitions to Voxelient.
 */
public class BlockTextureProvider implements BlockManager.IBlockTextureProvider {
    private final BlockRegistry blocks;

    public BlockTextureProvider() {
        this(OpenCraftBlockRegistry.blocks());
    }

    public BlockTextureProvider(BlockRegistry blocks) {
        this.blocks = Objects.requireNonNull(blocks, "blocks");
    }

    @Override
    public int getTexture(int blockType, int faceIndex) {
        return blocks.getTexture(blockType, faceIndex);
    }
}
