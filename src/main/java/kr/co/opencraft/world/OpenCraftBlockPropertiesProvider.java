package kr.co.opencraft.world;

import kr.co.voxelite.world.BlockManager;

/**
 * OpenCraft-specific block physics rules.
 */
public class OpenCraftBlockPropertiesProvider implements BlockManager.IBlockPropertiesProvider {
    @Override
    public boolean isSolid(int blockType) {
        return blockType != BlockTypes.AIR && blockType != BlockTypes.WATER;
    }
}
