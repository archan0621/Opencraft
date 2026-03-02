package kr.co.opencraft.world;

import kr.co.voxelite.world.Chunk;
import kr.co.voxelite.world.SimplexNoise;

/**
 * 바이옴 + lerp 기반 지형 생성
 * - Biome Noise (0.0005): 저주파, 넓은 구역, smoothstep
 * - Plains Noise (0.02): 고주파, 세밀한 평지
 * - Mountain Noise (0.008): 중저주파, 넓은 산맥, 기준 90+
 * - height = lerp(plainsHeight, mountainHeight, biomeWeight)
 */
public class TerrainGenerator {
    private final long seed;
    private final SimplexNoise noise;
    private final BiomeGenerator biomeGenerator;
    
    // ========== Octave Noise 파라미터 ==========
    private static final int OCTAVES = 4;
    private static final double PERSISTENCE = 0.55;
    private static final double LACUNARITY = 2.2;
    
    // ========== 평지 노이즈 (고주파 → 세밀하고 평평) ==========
    private static final double PLAINS_NOISE_SCALE = 0.02;
    private static final double PLAINS_AMPLITUDE = 5.0;   // 낮을수록 평평
    private static final int PLAINS_BASE_HEIGHT = 64;
    
    // ========== 산 노이즈 (중저주파 → 넓고 높은 산맥) ==========
    private static final double MOUNTAIN_NOISE_SCALE = 0.008;
    private static final double MOUNTAIN_AMPLITUDE = 45.0; // 높이 차이 극대화
    private static final int MOUNTAIN_BASE_HEIGHT = 92;
    
    // ========== 높이 제한 ==========
    private static final int MIN_TERRAIN_HEIGHT = 5;
    private static final int MAX_TERRAIN_HEIGHT = 120;
    
    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = new SimplexNoise(seed);
        this.biomeGenerator = new BiomeGenerator(seed);
    }
    
    /**
     * 청크에 지형 생성
     * - 바이옴별 높이·블록 구성 적용
     * - 레이어 구조: BEDROCK(0) → STONE(1~4) → 바이옴 블록(5~)
     */
    public void generateTerrain(Chunk chunk, int defaultBlockType) {
        long t0 = System.currentTimeMillis();
        int chunkX = chunk.getCoord().x;
        int chunkZ = chunk.getCoord().z;
        
        for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                int worldX = chunkX * Chunk.CHUNK_SIZE + localX;
                int worldZ = chunkZ * Chunk.CHUNK_SIZE + localZ;
                
                Biome biome = biomeGenerator.getBiomeAt(worldX, worldZ);
                double biomeWeight = biomeGenerator.getBiomeWeight(worldX, worldZ);
                
                // y=0: 기반암 (파괴 불가)
                chunk.addBlockLocal(localX, 0, localZ, BlockTypes.BEDROCK);
                
                // y=1~4: 돌 레이어 (기반 지형)
                for (int y = 1; y <= 4; y++) {
                    chunk.addBlockLocal(localX, y, localZ, biome.stoneBlock);
                }
                
                // y=5 이상: 바이옴 기반 지형 (lerp)
                int surfaceHeight = getHeight(worldX, worldZ, biomeWeight);
                
                for (int y = 5; y <= surfaceHeight; y++) {
                    if (y == surfaceHeight) {
                        chunk.addBlockLocal(localX, y, localZ, biome.surfaceBlock);
                    } else if (y >= surfaceHeight - 3) {
                        chunk.addBlockLocal(localX, y, localZ, biome.subsurfaceBlock);
                    } else {
                        chunk.addBlockLocal(localX, y, localZ, biome.stoneBlock);
                    }
                }
            }
        }
        
        chunk.markAsGenerated();
        long ms = System.currentTimeMillis() - t0;
        if (ms > 20) {
            System.out.printf("[PERF][TerrainGenerator] chunk(%d,%d): %d ms%n", chunkX, chunkZ, ms);
        }
    }
    
    /**
     * 평지/산 별도 노이즈 + biomeWeight lerp 기반 높이 계산
     * - plainsHeight: 고주파 노이즈 (0.02) → 세밀한 평지
     * - mountainHeight: 중저주파 노이즈 (0.008) → 넓은 산맥, 기준 90+
     * - biomeWeight: smoothstep 적용 (BiomeGenerator)
     */
    private int getHeight(int worldX, int worldZ, double biomeWeight) {
        // 평지: 고주파 노이즈 (서로 다른 노이즈)
        double plainsNoise = octaveNoise(worldX, worldZ, PLAINS_NOISE_SCALE, OCTAVES, PERSISTENCE, LACUNARITY);
        double plainsHeight = PLAINS_BASE_HEIGHT + plainsNoise * PLAINS_AMPLITUDE;
        
        // 산: 중저주파 노이즈 (서로 다른 노이즈)
        double mountainNoise = octaveNoise(worldX, worldZ, MOUNTAIN_NOISE_SCALE, OCTAVES, PERSISTENCE, LACUNARITY);
        double mountainHeight = MOUNTAIN_BASE_HEIGHT + mountainNoise * MOUNTAIN_AMPLITUDE;
        
        // lerp: biomeWeight에 smoothstep 이미 적용됨
        double height = plainsHeight + (mountainHeight - plainsHeight) * biomeWeight;
        
        int finalHeight = (int) Math.round(height);
        return Math.max(MIN_TERRAIN_HEIGHT, Math.min(MAX_TERRAIN_HEIGHT, finalHeight));
    }
    
    /**
     * Octave Noise (Fractal Brownian Motion)
     * 
     * @param baseScale 기본 스케일 (plains=0.02 고주파, mountain=0.008 중저주파)
     */
    private double octaveNoise(double x, double z, double baseScale, int octaves, double persistence, double lacunarity) {
        double total = 0.0;
        double frequency = baseScale;
        double amplitude = 1.0;
        double maxValue = 0.0;
        
        for (int i = 0; i < octaves; i++) {
            double noiseValue = noise.noise(x * frequency, z * frequency);
            total += noiseValue * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        
        return total / maxValue;
    }
    
    public long getSeed() {
        return seed;
    }
}
