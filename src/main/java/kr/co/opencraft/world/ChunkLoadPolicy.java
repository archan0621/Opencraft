package kr.co.opencraft.world;

/**
 * 청크 로딩 정책 (애플리케이션 레벨)
 * - 언제 로드/언로드할지
 * - 얼마나 메모리에 올릴지
 * - 사전 생성 거리
 */
public class ChunkLoadPolicy {
    private final int visibleDistance;       // 렌더 거리
    private final int keepLoadedDistance;    // 메모리 유지 거리
    private final int pregenerateDistance;   // 사전 생성 거리 (파일만)
    private final int maxLoadedChunks;       // 최대 메모리 청크
    
    public ChunkLoadPolicy(int visibleDistance, int keepLoadedDistance, int pregenerateDistance) {
        this.visibleDistance = Math.max(0, visibleDistance);
        this.keepLoadedDistance = Math.max(this.visibleDistance, keepLoadedDistance);
        this.pregenerateDistance = Math.max(this.keepLoadedDistance, pregenerateDistance);
        this.maxLoadedChunks = calculateMaxChunks(this.keepLoadedDistance);
    }
    
    /**
     * 렌더 거리 내 청크를 화면에 보여줘야 하는가?
     */
    public boolean shouldLoadToMemory(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);
        return dx <= visibleDistance && dz <= visibleDistance;
    }

    /**
     * 렌더 범위 밖이어도 메모리에 유지해야 하는가?
     */
    public boolean shouldKeepLoaded(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);
        return dx <= keepLoadedDistance && dz <= keepLoadedDistance;
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
    private int calculateMaxChunks(int keepLoadedDistance) {
        int baseChunks = (keepLoadedDistance * 2 + 1) * (keepLoadedDistance * 2 + 1);
        return baseChunks + 50; // 여유분
    }
    
    // Getters
    public int getVisibleDistance() {
        return visibleDistance;
    }

    public int getKeepLoadedDistance() {
        return keepLoadedDistance;
    }

    public int getRenderDistance() {
        return visibleDistance;
    }
    
    public int getPregenerateDistance() {
        return pregenerateDistance;
    }
    
    public int getMaxLoadedChunks() {
        return maxLoadedChunks;
    }
}
