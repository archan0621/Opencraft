package kr.co.opencraft.world;

import kr.co.voxelite.world.Chunk;
import kr.co.voxelite.world.SimplexNoise;

/**
 * Multi-noise terrain generation:
 * - continentalness for large terrain regions
 * - fBm for large + medium + small landforms
 * - ridge noise for mountain chains and valleys
 * - erosion for natural attenuation
 * - plateau shaping and summit smoothing
 */
public class TerrainGenerator {
    private static final TerrainConfig CONFIG = TerrainConfig.createDefault();

    private final long seed;
    private final SimplexNoise noise;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = new SimplexNoise(seed);
    }

    /**
     * Chunk generation remains column-based for chunk compatibility.
     */
    public void generateTerrain(Chunk chunk, int defaultBlockType) {
        long t0 = System.currentTimeMillis();
        int chunkX = chunk.getCoord().x;
        int chunkZ = chunk.getCoord().z;

        for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                int worldX = chunkX * Chunk.CHUNK_SIZE + localX;
                int worldZ = chunkZ * Chunk.CHUNK_SIZE + localZ;
                TerrainSample sample = sampleTerrain(worldX, worldZ);
                Biome biome = sample.biome;

                chunk.addBlockLocal(localX, 0, localZ, BlockTypes.BEDROCK);
                for (int y = 1; y <= 4; y++) {
                    chunk.addBlockLocal(localX, y, localZ, biome.stoneBlock);
                }

                for (int y = 5; y <= sample.height; y++) {
                    if (y == sample.height) {
                        chunk.addBlockLocal(localX, y, localZ, biome.surfaceBlock);
                    } else if (y >= sample.height - 3) {
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

    private TerrainSample sampleTerrain(int worldX, int worldZ) {
        double warpX = sampleFbm(worldX + 7000.0, worldZ - 7000.0, CONFIG.warpLayer) * CONFIG.warpStrength;
        double warpZ = sampleFbm(worldX - 7000.0, worldZ + 7000.0, CONFIG.warpLayer) * CONFIG.warpStrength;
        double sampleX = worldX + warpX;
        double sampleZ = worldZ + warpZ;

        double continent = sampleFbm(sampleX, sampleZ, CONFIG.continentLayer);
        double baseTerrain = sampleLayer(sampleX, sampleZ, CONFIG.baseLargeLayer)
            + sampleLayer(sampleX, sampleZ, CONFIG.baseMediumLayer)
            + sampleLayer(sampleX, sampleZ, CONFIG.baseDetailLayer);

        double ridgePrimary = sampleRidged(sampleX + 1800.0, sampleZ - 1800.0, CONFIG.primaryRidgeLayer);
        double ridgeSecondary = sampleRidged(sampleX - 2600.0, sampleZ + 2600.0, CONFIG.secondaryRidgeLayer);
        double ridge = Math.max(
            smoothstep(Math.pow(clamp01(ridgePrimary), 1.15)),
            smoothstep(Math.pow(clamp01(ridgeSecondary), 1.35)) * 0.84
        );

        double erosion = normalized(sampleFbm(sampleX + 3200.0, sampleZ - 3200.0, CONFIG.erosionLayer));
        double erosionFactor = lerp(CONFIG.erosionMinMultiplier, CONFIG.erosionMaxMultiplier, erosion);

        double plateauNoise = normalized(sampleFbm(sampleX - 4800.0, sampleZ + 4800.0, CONFIG.plateauLayer));
        double landBlend = smoothstepRange(continent, CONFIG.oceanStart, CONFIG.oceanEnd);
        double foothillWeight = smoothstepRange(continent, CONFIG.plainsCenter, CONFIG.hillsCenter);
        double mountainWeight = smoothstepRange(continent, CONFIG.mountainsStart, CONFIG.mountainsEnd);
        double oceanHeight = CONFIG.oceanFloorHeight + baseTerrain * 0.18;
        double plainsHeight = CONFIG.plainsBaseHeight + baseTerrain * 0.12 + erosion * 2.0;
        double hillsHeight = CONFIG.hillsBaseHeight + baseTerrain * 0.20 + ridge * 5.0 * erosionFactor;
        double landHeight = lerp(plainsHeight, hillsHeight, foothillWeight);

        double mountainBase = landHeight + mountainWeight * (CONFIG.mountainBaseHeight - CONFIG.hillsBaseHeight);
        double ridgeMountains = ridge * CONFIG.primaryRidgeAmplitude + ridgeSecondary * CONFIG.secondaryRidgeAmplitude;
        double mountainUplift = ridgeMountains * Math.pow(mountainWeight, 1.65) * erosionFactor;
        double mountainHeight = mountainBase + mountainUplift;

        double plateauMask = mountainWeight
            * smoothstepRange(plateauNoise, CONFIG.plateauStart, CONFIG.plateauEnd)
            * smoothstepRange(ridge, 0.68, 0.90);
        double plateauTarget = roundToStep(mountainHeight, CONFIG.plateauStepHeight);
        mountainHeight = lerp(mountainHeight, plateauTarget, plateauMask * CONFIG.plateauStrength);

        double summitSmoothMask = mountainWeight * smoothstepRange(ridge, 0.72, 0.94);
        double smoothedSummit = mountainBase + ridgeMountains * 0.78;
        mountainHeight = lerp(mountainHeight, smoothedSummit, summitSmoothMask * CONFIG.summitSmoothingStrength);

        double valleyCarve = (1.0 - ridge) * (1.0 - erosion) * Math.pow(mountainWeight, 1.25);
        mountainHeight -= valleyCarve * CONFIG.valleyDepth;

        double terrainHeight = lerp(landHeight, mountainHeight, mountainWeight);
        double finalHeight = lerp(oceanHeight, terrainHeight, landBlend);

        int height = clampHeight((int) Math.round(finalHeight));
        return new TerrainSample(height, selectBiome(landBlend, foothillWeight, mountainWeight, erosion));
    }

    private Biome selectBiome(double landBlend, double foothillWeight, double mountainWeight, double erosion) {
        if (mountainWeight > 0.42) {
            return Biome.MOUNTAIN;
        }
        if (foothillWeight > 0.45) {
            return Biome.HILLS;
        }
        if (landBlend < 0.35) {
            return Biome.PLAINS;
        }
        return erosion > 0.56 ? Biome.FOREST : Biome.PLAINS;
    }

    private int clampHeight(int height) {
        return Math.max(CONFIG.minTerrainHeight, Math.min(CONFIG.maxTerrainHeight, height));
    }

    private double sampleLayer(double x, double z, NoiseLayer layer) {
        return sampleFbm(x, z, layer) * layer.amplitude;
    }

    private double sampleFbm(double x, double z, NoiseLayer layer) {
        double total = 0.0;
        double frequency = layer.scale;
        double amplitude = 1.0;
        double maxValue = 0.0;

        for (int i = 0; i < layer.octaves; i++) {
            total += noise.noise(x * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= layer.persistence;
            frequency *= layer.lacunarity;
        }

        return maxValue == 0.0 ? 0.0 : total / maxValue;
    }

    private double sampleRidged(double x, double z, NoiseLayer layer) {
        double total = 0.0;
        double frequency = layer.scale;
        double amplitude = 1.0;
        double maxValue = 0.0;

        for (int i = 0; i < layer.octaves; i++) {
            double ridge = 1.0 - Math.abs(noise.noise(x * frequency, z * frequency));
            ridge *= ridge;
            total += ridge * amplitude;
            maxValue += amplitude;
            amplitude *= layer.persistence;
            frequency *= layer.lacunarity;
        }

        return maxValue == 0.0 ? 0.0 : total / maxValue;
    }

    private double normalized(double value) {
        return clamp01((value + 1.0) * 0.5);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * clamp01(t);
    }

    private double smoothstep(double t) {
        double clamped = clamp01(t);
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private double smoothstepRange(double value, double edge0, double edge1) {
        if (edge0 == edge1) {
            return value < edge0 ? 0.0 : 1.0;
        }
        return smoothstep((value - edge0) / (edge1 - edge0));
    }

    private double smoothBand(double value, double start, double center, double end) {
        double rise = smoothstepRange(value, start, center);
        double fall = 1.0 - smoothstepRange(value, center, end);
        return clamp01(Math.min(rise, fall));
    }

    private double roundToStep(double value, double step) {
        if (step <= 0.0) {
            return value;
        }
        return Math.round(value / step) * step;
    }

    public long getSeed() {
        return seed;
    }

    private static final class TerrainSample {
        private final int height;
        private final Biome biome;

        private TerrainSample(int height, Biome biome) {
            this.height = height;
            this.biome = biome;
        }
    }

    private static final class NoiseLayer {
        private final double scale;
        private final double amplitude;
        private final int octaves;
        private final double persistence;
        private final double lacunarity;

        private NoiseLayer(double scale, double amplitude, int octaves, double persistence, double lacunarity) {
            this.scale = scale;
            this.amplitude = amplitude;
            this.octaves = octaves;
            this.persistence = persistence;
            this.lacunarity = lacunarity;
        }
    }

    private static final class TerrainConfig {
        private final NoiseLayer warpLayer;
        private final double warpStrength;
        private final NoiseLayer continentLayer;
        private final NoiseLayer baseLargeLayer;
        private final NoiseLayer baseMediumLayer;
        private final NoiseLayer baseDetailLayer;
        private final NoiseLayer primaryRidgeLayer;
        private final NoiseLayer secondaryRidgeLayer;
        private final NoiseLayer erosionLayer;
        private final NoiseLayer plateauLayer;
        private final double erosionMinMultiplier;
        private final double erosionMaxMultiplier;
        private final double oceanStart;
        private final double oceanEnd;
        private final double plainsStart;
        private final double plainsCenter;
        private final double hillsStart;
        private final double hillsCenter;
        private final double mountainsStart;
        private final double mountainsEnd;
        private final int minTerrainHeight;
        private final int maxTerrainHeight;
        private final double oceanFloorHeight;
        private final double plainsBaseHeight;
        private final double hillsBaseHeight;
        private final double mountainBaseHeight;
        private final double primaryRidgeAmplitude;
        private final double secondaryRidgeAmplitude;
        private final double valleyDepth;
        private final double plateauStart;
        private final double plateauEnd;
        private final double plateauStepHeight;
        private final double plateauStrength;
        private final double summitSmoothingStrength;

        private TerrainConfig(
            NoiseLayer warpLayer,
            double warpStrength,
            NoiseLayer continentLayer,
            NoiseLayer baseLargeLayer,
            NoiseLayer baseMediumLayer,
            NoiseLayer baseDetailLayer,
            NoiseLayer primaryRidgeLayer,
            NoiseLayer secondaryRidgeLayer,
            NoiseLayer erosionLayer,
            NoiseLayer plateauLayer,
            double erosionMinMultiplier,
            double erosionMaxMultiplier,
            double oceanStart,
            double oceanEnd,
            double plainsStart,
            double plainsCenter,
            double hillsStart,
            double hillsCenter,
            double mountainsStart,
            double mountainsEnd,
            int minTerrainHeight,
            int maxTerrainHeight,
            double oceanFloorHeight,
            double plainsBaseHeight,
            double hillsBaseHeight,
            double mountainBaseHeight,
            double primaryRidgeAmplitude,
            double secondaryRidgeAmplitude,
            double valleyDepth,
            double plateauStart,
            double plateauEnd,
            double plateauStepHeight,
            double plateauStrength,
            double summitSmoothingStrength
        ) {
            this.warpLayer = warpLayer;
            this.warpStrength = warpStrength;
            this.continentLayer = continentLayer;
            this.baseLargeLayer = baseLargeLayer;
            this.baseMediumLayer = baseMediumLayer;
            this.baseDetailLayer = baseDetailLayer;
            this.primaryRidgeLayer = primaryRidgeLayer;
            this.secondaryRidgeLayer = secondaryRidgeLayer;
            this.erosionLayer = erosionLayer;
            this.plateauLayer = plateauLayer;
            this.erosionMinMultiplier = erosionMinMultiplier;
            this.erosionMaxMultiplier = erosionMaxMultiplier;
            this.oceanStart = oceanStart;
            this.oceanEnd = oceanEnd;
            this.plainsStart = plainsStart;
            this.plainsCenter = plainsCenter;
            this.hillsStart = hillsStart;
            this.hillsCenter = hillsCenter;
            this.mountainsStart = mountainsStart;
            this.mountainsEnd = mountainsEnd;
            this.minTerrainHeight = minTerrainHeight;
            this.maxTerrainHeight = maxTerrainHeight;
            this.oceanFloorHeight = oceanFloorHeight;
            this.plainsBaseHeight = plainsBaseHeight;
            this.hillsBaseHeight = hillsBaseHeight;
            this.mountainBaseHeight = mountainBaseHeight;
            this.primaryRidgeAmplitude = primaryRidgeAmplitude;
            this.secondaryRidgeAmplitude = secondaryRidgeAmplitude;
            this.valleyDepth = valleyDepth;
            this.plateauStart = plateauStart;
            this.plateauEnd = plateauEnd;
            this.plateauStepHeight = plateauStepHeight;
            this.plateauStrength = plateauStrength;
            this.summitSmoothingStrength = summitSmoothingStrength;
        }

        private static TerrainConfig createDefault() {
            return new TerrainConfig(
                new NoiseLayer(0.0018, 1.0, 2, 0.5, 2.0),
                16.0,
                new NoiseLayer(0.0010, 1.0, 3, 0.5, 2.0),
                new NoiseLayer(0.010, 40.0, 3, 0.5, 2.0),
                new NoiseLayer(0.020, 20.0, 2, 0.5, 2.0),
                new NoiseLayer(0.040, 10.0, 2, 0.45, 2.0),
                new NoiseLayer(0.0060, 1.0, 4, 0.55, 2.0),
                new NoiseLayer(0.0125, 1.0, 3, 0.5, 2.1),
                new NoiseLayer(0.020, 1.0, 2, 0.5, 2.0),
                new NoiseLayer(0.0035, 1.0, 2, 0.55, 2.0),
                0.35,
                1.0,
                -0.32,
                -0.08,
                -0.05,
                0.10,
                0.22,
                0.38,
                0.55,
                0.82,
                5,
                255,
                42.0,
                62.0,
                74.0,
                90.0,
                42.0,
                14.0,
                8.0,
                0.68,
                0.86,
                4.0,
                0.35,
                0.42
            );
        }
    }
}
