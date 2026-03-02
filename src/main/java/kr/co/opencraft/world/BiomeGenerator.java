package kr.co.opencraft.world;

import kr.co.voxelite.world.SimplexNoise;

/**
 * 노이즈 기반 바이옴 결정
 * - 저주파(0.0005) 노이즈로 넓은 바이옴 구역 생성
 * - getBiomeWeight: 0~1 연속값 (smoothstep) → height lerp용
 * - getBiomeAt: 블록 선택용 이산 바이옴
 */
public class BiomeGenerator {
    private final SimplexNoise noise;
    
    /** 바이옴 구역 노이즈 스케일 (저주파 → 넓은 구역) */
    private static final double BIOME_SCALE = 0.0005;
    
    /** 2차 노이즈 오프셋 (다양한 조합) */
    private static final double BIOME_OFFSET = 1000;
    
    public BiomeGenerator(long seed) {
        this.noise = new SimplexNoise(seed);
    }
    
    /**
     * 바이옴 가중치 (0~1) - height lerp용
     * 구역 기반: 평지/산 구역을 넓히고, 전환 구간만 lerp
     */
    public double getBiomeWeight(int worldX, int worldZ) {
        double n1 = noise.noise(worldX * BIOME_SCALE, worldZ * BIOME_SCALE);
        double n2 = noise.noise(worldX * BIOME_SCALE + BIOME_OFFSET, worldZ * BIOME_SCALE + BIOME_OFFSET);
        
        double raw = (n1 + 1) * 0.5 * 0.6 + (n2 + 1) * 0.5 * 0.4;
        raw = Math.max(0, Math.min(1, raw));
        
        // 구역: raw < 0.38 → 순수 평지, raw > 0.62 → 순수 산, 그 사이만 전환
        if (raw < 0.38) {
            return 0;
        } else if (raw > 0.62) {
            return 1;
        } else {
            return smoothstep((raw - 0.38) / 0.24);
        }
    }
    
    /** smoothstep(t) = 3t² - 2t³ */
    private static double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }
    
    /** 블록 선택용 이산 바이옴 */
    public Biome getBiomeAt(int worldX, int worldZ) {
        double weight = getBiomeWeight(worldX, worldZ);
        if (weight < 0.25) return Biome.PLAINS;
        if (weight < 0.5) return Biome.FOREST;
        if (weight < 0.75) return Biome.HILLS;
        return Biome.MOUNTAIN;
    }
}
