package kr.co.opencraft.world;

import kr.co.voxelite.world.SimplexNoise;

public class BiomeGenerator {
    private static final double CONTINENTAL_SCALE = 0.0018;
    private static final double HUMIDITY_SCALE = 0.0015;
    private static final double PEAK_SCALE = 0.003;

    private final SimplexNoise continentalNoise;
    private final SimplexNoise humidityNoise;
    private final SimplexNoise peakNoise;

    public BiomeGenerator(long seed) {
        this.continentalNoise = new SimplexNoise(seed);
        this.humidityNoise = new SimplexNoise(seed ^ 0x9E3779B97F4A7C15L);
        this.peakNoise = new SimplexNoise(seed ^ 0xC2B2AE3D27D4EB4FL);
    }

    public double getBiomeWeight(int worldX, int worldZ) {
        double continental = normalized(continentalNoise.noise(worldX * CONTINENTAL_SCALE, worldZ * CONTINENTAL_SCALE));
        double peaks = normalized(peakNoise.noise(worldX * PEAK_SCALE + 1700.0, worldZ * PEAK_SCALE - 1700.0));
        return clamp01(continental * 0.6 + peaks * 0.4);
    }

    public Biome getBiomeAt(int worldX, int worldZ) {
        double continental = normalized(continentalNoise.noise(worldX * CONTINENTAL_SCALE, worldZ * CONTINENTAL_SCALE));
        double humidity = normalized(humidityNoise.noise(worldX * HUMIDITY_SCALE - 2500.0, worldZ * HUMIDITY_SCALE + 2500.0));
        double peaks = normalized(peakNoise.noise(worldX * PEAK_SCALE + 1700.0, worldZ * PEAK_SCALE - 1700.0));

        if (continental > 0.68 && peaks > 0.74) {
            return Biome.MOUNTAIN;
        }
        if (continental > 0.52 || peaks > 0.61) {
            return Biome.HILLS;
        }
        if (humidity > 0.54) {
            return Biome.FOREST;
        }
        return Biome.PLAINS;
    }

    private double normalized(double value) {
        return clamp01((value + 1.0) * 0.5);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
