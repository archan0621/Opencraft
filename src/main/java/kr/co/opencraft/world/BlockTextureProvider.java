package kr.co.opencraft.world;

import kr.co.voxelite.world.BlockManager;

/**
 * Adapter that connects BlockTextures to Voxelite's BlockManager
 */
public class BlockTextureProvider implements BlockManager.IBlockTextureProvider {
    
    @Override
    public int getTexture(int blockType, int faceIndex) {
        return BlockTextures.getTexture(blockType, faceIndex);
    }
}
