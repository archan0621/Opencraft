package kr.co.opencraft.world;

import kr.co.voxelite.world.Chunk;
import kr.co.voxelite.world.SimplexNoise;

/**
 * 지형 생성 정책 (애플리케이션 레벨)
 * - 어떤 지형을 만들지 결정
 * - SimplexNoise 사용 방법
 * - 높이 범위, 스케일 등
 */
public class TerrainGenerator {
    private final long seed;
    private final SimplexNoise noise;
    
    // 지형 설정
    private static final double NOISE_SCALE = 0.05; // 작을수록 부드러운 지형
    private static final int MIN_HEIGHT = 0;
    private static final int MAX_HEIGHT = 3;
    private static final float BASE_Y = 0f; // 지형 시작 높이 (y=0이 바닥)
    
    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = new SimplexNoise(seed);
    }
    
    /**
     * 청크에 지형 생성
     * - 읽기 쉬운 코드: 무엇을 하는지 명확
     */
    public void generateTerrain(Chunk chunk, int blockType) {
        int chunkX = chunk.getCoord().x;
        int chunkZ = chunk.getCoord().z;
        
        for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                int height = calculateHeight(chunkX, chunkZ, localX, localZ);
                placeBlocks(chunk, localX, localZ, height, blockType);
            }
        }
        
        chunk.markAsGenerated();
    }
    
    /**
     * 노이즈 기반 높이 계산
     */
    private int calculateHeight(int chunkX, int chunkZ, int localX, int localZ) {
        float worldX = chunkX * Chunk.CHUNK_SIZE + localX;
        float worldZ = chunkZ * Chunk.CHUNK_SIZE + localZ;
        
        double noiseValue = noise.noise(worldX * NOISE_SCALE, worldZ * NOISE_SCALE);
        
        // -1~1 범위를 MIN_HEIGHT~MAX_HEIGHT로 변환
        int height = (int) ((noiseValue + 1.0) / 2.0 * (MAX_HEIGHT - MIN_HEIGHT)) + MIN_HEIGHT;
        return Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, height));
    }
    
    /**
     * 블록 배치
     */
    private void placeBlocks(Chunk chunk, int localX, int localZ, int height, int blockType) {
        for (int y = 0; y <= height; y++) {
            float worldY = y + BASE_Y;
            chunk.addBlockLocal(localX, worldY, localZ, blockType);
        }
    }
    
    public long getSeed() {
        return seed;
    }
}
