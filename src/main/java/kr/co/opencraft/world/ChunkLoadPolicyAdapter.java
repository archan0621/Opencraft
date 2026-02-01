package kr.co.opencraft.world;

import kr.co.voxelite.world.IChunkLoadPolicy;

/**
 * ChunkLoadPolicy를 라이브러리 인터페이스에 맞게 변환
 */
public class ChunkLoadPolicyAdapter implements IChunkLoadPolicy {
    private final ChunkLoadPolicy policy;
    
    public ChunkLoadPolicyAdapter(ChunkLoadPolicy policy) {
        this.policy = policy;
    }
    
    @Override
    public boolean shouldLoadToMemory(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
        return policy.shouldLoadToMemory(chunkX, chunkZ, playerChunkX, playerChunkZ);
    }
    
    @Override
    public boolean shouldPregenerate(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
        return policy.shouldPregenerate(chunkX, chunkZ, playerChunkX, playerChunkZ);
    }
    
    @Override
    public int getMaxLoadedChunks() {
        return policy.getMaxLoadedChunks();
    }
}
