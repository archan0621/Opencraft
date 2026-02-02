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
    private static final int DIRT_MIN_HEIGHT = 5; // dirt 시작 높이
    private static final int DIRT_MAX_HEIGHT = 8; // dirt 최대 높이
    private static final float BASE_Y = 0f; // 지형 시작 높이 (y=0이 바닥)
    
    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = new SimplexNoise(seed);
    }
    
    /**
     * 청크에 지형 생성
     * - 읽기 쉬운 코드: 무엇을 하는지 명확
     */
    public void generateTerrain(Chunk chunk, int defaultBlockType) {
        int chunkX = chunk.getCoord().x;
        int chunkZ = chunk.getCoord().z;
        
        for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                // y=0: 배드락
                chunk.addBlockLocal(localX, 0f, localZ, BlockTypes.BEDROCK);
                
                // y=1~4: stone
                for (int y = 1; y <= 4; y++) {
                    chunk.addBlockLocal(localX, y, localZ, BlockTypes.MY_STONE);
                }
                
                // y=5 이상: 노이즈 기반 dirt (땅)
                int dirtHeight = calculateDirtHeight(chunkX, chunkZ, localX, localZ);
                for (int y = 5; y <= dirtHeight; y++) {
                    chunk.addBlockLocal(localX, y, localZ, BlockTypes.DIRT);
                }
            }
        }
        
        chunk.markAsGenerated();
    }
    
    /**
     * 노이즈 기반 dirt 높이 계산
     */
    private int calculateDirtHeight(int chunkX, int chunkZ, int localX, int localZ) {
        float worldX = chunkX * Chunk.CHUNK_SIZE + localX;
        float worldZ = chunkZ * Chunk.CHUNK_SIZE + localZ;
        
        double noiseValue = noise.noise(worldX * NOISE_SCALE, worldZ * NOISE_SCALE);
        
        // -1~1 범위를 DIRT_MIN_HEIGHT~DIRT_MAX_HEIGHT로 변환
        int height = (int) ((noiseValue + 1.0) / 2.0 * (DIRT_MAX_HEIGHT - DIRT_MIN_HEIGHT)) + DIRT_MIN_HEIGHT;
        return Math.max(DIRT_MIN_HEIGHT, Math.min(DIRT_MAX_HEIGHT, height));
    }
    
    public long getSeed() {
        return seed;
    }
}
