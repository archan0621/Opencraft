package kr.co.opencraft.world;

/**
 * 청크 로딩 정책 (애플리케이션 레벨)
 * - 언제 로드/언로드할지
 * - 얼마나 메모리에 올릴지
 * - 사전 생성 거리
 */
public class ChunkLoadPolicy {
    private final int renderDistance;        // 렌더 거리 (메모리 로드)
    private final int pregenerateDistance;   // 사전 생성 거리 (파일만)
    private final int maxLoadedChunks;       // 최대 메모리 청크
    
    public ChunkLoadPolicy(int renderDistance, int pregenerateDistance) {
        this.renderDistance = renderDistance;
        this.pregenerateDistance = pregenerateDistance;
        this.maxLoadedChunks = calculateMaxChunks(renderDistance);
    }
    
    /**
     * 렌더 거리 내 청크를 메모리에 로드해야 하는가?
     */
    public boolean shouldLoadToMemory(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);
        return dx <= renderDistance && dz <= renderDistance;
    }
    
    /**
     * 이 청크를 사전 생성해야 하는가?
     */
    public boolean shouldPregenerate(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);
        return dx <= pregenerateDistance && dz <= pregenerateDistance;
    }
    
    /**
     * 최대 로드 청크 수 계산
     */
    private int calculateMaxChunks(int renderDistance) {
        int baseChunks = (renderDistance * 2 + 1) * (renderDistance * 2 + 1);
        return baseChunks + 50; // 여유분
    }
    
    // Getters
    public int getRenderDistance() {
        return renderDistance;
    }
    
    public int getPregenerateDistance() {
        return pregenerateDistance;
    }
    
    public int getMaxLoadedChunks() {
        return maxLoadedChunks;
    }
}
