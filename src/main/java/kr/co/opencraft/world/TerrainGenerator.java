package kr.co.opencraft.world;

import kr.co.opencraft.world.noise.OctaveNoise;
import kr.co.voxelite.world.Chunk;

import java.util.Random;

/**
 * Minecraft 1.12 overworld-style terrain generation without water or structures.
 * The flow mirrors the vanilla generator at a high level:
 * biome blend -> coarse density field -> chunk fill -> surface replacement.
 */
public class TerrainGenerator {
    private static final int CHUNK_HEIGHT = 256;
    private static final int COARSE_GRID_SIZE = 5;
    private static final int BIOME_GENERATION_GRID_SIZE = 10;
    private static final int COARSE_HEIGHT_SAMPLES = 33;
    private static final int COARSE_HORIZONTAL_STEP = 4;
    private static final int COARSE_VERTICAL_STEP = 8;
    private static final int SEA_LEVEL = 63;

    private static final double COORDINATE_SCALE = 684.412;
    private static final double HEIGHT_SCALE = 684.412;
    private static final double UPPER_LIMIT_SCALE = 512.0;
    private static final double LOWER_LIMIT_SCALE = 512.0;
    private static final double DEPTH_NOISE_SCALE_X = 200.0;
    private static final double DEPTH_NOISE_SCALE_Z = 200.0;
    private static final double DEPTH_NOISE_SCALE_EXPONENT = 0.5;
    private static final double MAIN_NOISE_SCALE_X = 80.0;
    private static final double MAIN_NOISE_SCALE_Y = 160.0;
    private static final double MAIN_NOISE_SCALE_Z = 80.0;
    private static final double BASE_SIZE = 8.5;
    private static final double STRETCH_Y = 12.0;
    private static final double BIOME_DEPTH_WEIGHT = 1.0;
    private static final double BIOME_SCALE_WEIGHT = 1.0;
    private static final double BIOME_DEPTH_OFFSET = 0.0;
    private static final double BIOME_SCALE_OFFSET = 0.0;
    private static final double SURFACE_NOISE_SCALE = 0.0625;

    private final long seed;
    private final BiomeGenerator biomeGenerator;
    private final OctaveNoise minLimitNoise;
    private final OctaveNoise maxLimitNoise;
    private final OctaveNoise mainNoise;
    private final OctaveNoise surfaceNoise;
    private final OctaveNoise depthNoise;
    private final double[] heightMap;
    private final float[] biomeWeights;
    private double[] mainNoiseRegion;
    private double[] minLimitRegion;
    private double[] maxLimitRegion;
    private double[] depthRegion;
    private double[] surfaceDepthBuffer;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.biomeGenerator = new BiomeGenerator(seed);
        this.heightMap = new double[COARSE_GRID_SIZE * COARSE_HEIGHT_SAMPLES * COARSE_GRID_SIZE];
        this.biomeWeights = new float[25];

        Random random = new Random(seed);
        this.minLimitNoise = new OctaveNoise(random, 16);
        this.maxLimitNoise = new OctaveNoise(random, 16);
        this.mainNoise = new OctaveNoise(random, 8);
        this.surfaceNoise = new OctaveNoise(random, 4);
        this.depthNoise = new OctaveNoise(random, 16);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                float weight = 10.0F / (float) Math.sqrt(dx * dx + dz * dz + 0.2F);
                biomeWeights[dx + 2 + (dz + 2) * 5] = weight;
            }
        }
    }

    public void generateTerrain(Chunk chunk, int defaultBlockType) {
        int chunkX = chunk.getCoord().x;
        int chunkZ = chunk.getCoord().z;
        Biome[] biomesForGeneration = sampleBiomesForGeneration(chunkX, chunkZ);

        setBlocksInChunk(chunk, chunkX, chunkZ, biomesForGeneration);
        replaceBiomeBlocks(chunk, chunkX, chunkZ);
        chunk.markAsGenerated();
    }

    private void setBlocksInChunk(Chunk chunk, int chunkX, int chunkZ, Biome[] biomesForGeneration) {
        generateHeightMap(chunkX * COARSE_HORIZONTAL_STEP, chunkZ * COARSE_HORIZONTAL_STEP, biomesForGeneration);

        for (int gridX = 0; gridX < 4; gridX++) {
            int rowStart = gridX * 5;
            int rowEnd = (gridX + 1) * 5;

            for (int gridZ = 0; gridZ < 4; gridZ++) {
                int index000 = (rowStart + gridZ) * COARSE_HEIGHT_SAMPLES;
                int index001 = (rowStart + gridZ + 1) * COARSE_HEIGHT_SAMPLES;
                int index100 = (rowEnd + gridZ) * COARSE_HEIGHT_SAMPLES;
                int index101 = (rowEnd + gridZ + 1) * COARSE_HEIGHT_SAMPLES;

                for (int gridY = 0; gridY < 32; gridY++) {
                    double density000 = heightMap[index000 + gridY];
                    double density001 = heightMap[index001 + gridY];
                    double density100 = heightMap[index100 + gridY];
                    double density101 = heightMap[index101 + gridY];
                    double densityStep000 = (heightMap[index000 + gridY + 1] - density000) * 0.125D;
                    double densityStep001 = (heightMap[index001 + gridY + 1] - density001) * 0.125D;
                    double densityStep100 = (heightMap[index100 + gridY + 1] - density100) * 0.125D;
                    double densityStep101 = (heightMap[index101 + gridY + 1] - density101) * 0.125D;

                    for (int subY = 0; subY < COARSE_VERTICAL_STEP; subY++) {
                        double densityX0 = density000;
                        double densityX1 = density001;
                        double densityXStep0 = (density100 - density000) * 0.25D;
                        double densityXStep1 = (density101 - density001) * 0.25D;

                        for (int subX = 0; subX < COARSE_HORIZONTAL_STEP; subX++) {
                            double densityZStep = (densityX1 - densityX0) * 0.25D;
                            double density = densityX0 - densityZStep;

                            for (int subZ = 0; subZ < COARSE_HORIZONTAL_STEP; subZ++) {
                                density += densityZStep;
                                if (density > 0.0D) {
                                    chunk.addBlockLocal(
                                        gridX * COARSE_HORIZONTAL_STEP + subX,
                                        gridY * COARSE_VERTICAL_STEP + subY,
                                        gridZ * COARSE_HORIZONTAL_STEP + subZ,
                                        BlockTypes.MY_STONE
                                    );
                                }
                            }

                            densityX0 += densityXStep0;
                            densityX1 += densityXStep1;
                        }

                        density000 += densityStep000;
                        density001 += densityStep001;
                        density100 += densityStep100;
                        density101 += densityStep101;
                    }
                }
            }
        }
    }

    private void replaceBiomeBlocks(Chunk chunk, int chunkX, int chunkZ) {
        surfaceDepthBuffer = surfaceNoise.generateNoise(
            surfaceDepthBuffer,
            chunkX * Chunk.CHUNK_SIZE,
            chunkZ * Chunk.CHUNK_SIZE,
            Chunk.CHUNK_SIZE,
            Chunk.CHUNK_SIZE,
            SURFACE_NOISE_SCALE,
            SURFACE_NOISE_SCALE,
            1.0D
        );

        for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                int worldX = chunkX * Chunk.CHUNK_SIZE + localX;
                int worldZ = chunkZ * Chunk.CHUNK_SIZE + localZ;
                Biome biome = biomeGenerator.getBiomeAt(worldX, worldZ);
                double surfaceValue = surfaceDepthBuffer[localZ + localX * Chunk.CHUNK_SIZE];
                replaceSurfaceColumn(chunk, localX, localZ, worldX, worldZ, biome, surfaceValue);
            }
        }
    }

    private void replaceSurfaceColumn(
        Chunk chunk,
        int localX,
        int localZ,
        int worldX,
        int worldZ,
        Biome biome,
        double surfaceValue
    ) {
        int surfaceDepth = Math.max(1, (int) (surfaceValue / 3.0D + 3.0D + columnRandom(worldX, worldZ, 0) * 0.25D));
        int remainingDepth = -1;

        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (y <= bedrockLevel(worldX, worldZ, y)) {
                chunk.addBlockLocal(localX, y, localZ, BlockTypes.BEDROCK);
                continue;
            }

            Chunk.BlockData current = chunk.getBlock(localX, y, localZ);
            if (current == null) {
                remainingDepth = -1;
                continue;
            }

            if (current.blockType != BlockTypes.MY_STONE) {
                continue;
            }

            if (remainingDepth == -1) {
                remainingDepth = surfaceDepth;
                if (surfaceDepth <= 0) {
                    chunk.addBlockLocal(localX, y, localZ, biome.stoneBlock);
                } else if (y >= stoneExposureHeight(biome)) {
                    chunk.addBlockLocal(localX, y, localZ, biome.stoneBlock);
                } else {
                    chunk.addBlockLocal(localX, y, localZ, biome.surfaceBlock);
                }
            } else if (remainingDepth > 0) {
                chunk.addBlockLocal(localX, y, localZ, biome.subsurfaceBlock);
                remainingDepth--;
            } else {
                chunk.addBlockLocal(localX, y, localZ, biome.stoneBlock);
            }
        }
    }

    private void generateHeightMap(int coarseX, int coarseZ, Biome[] biomesForGeneration) {
        depthRegion = depthNoise.generateNoise(
            depthRegion,
            coarseX,
            coarseZ,
            COARSE_GRID_SIZE,
            COARSE_GRID_SIZE,
            DEPTH_NOISE_SCALE_X,
            DEPTH_NOISE_SCALE_Z,
            DEPTH_NOISE_SCALE_EXPONENT
        );
        mainNoiseRegion = mainNoise.generateNoise(
            mainNoiseRegion,
            coarseX,
            0,
            coarseZ,
            COARSE_GRID_SIZE,
            COARSE_HEIGHT_SAMPLES,
            COARSE_GRID_SIZE,
            COORDINATE_SCALE / MAIN_NOISE_SCALE_X,
            HEIGHT_SCALE / MAIN_NOISE_SCALE_Y,
            COORDINATE_SCALE / MAIN_NOISE_SCALE_Z
        );
        minLimitRegion = minLimitNoise.generateNoise(
            minLimitRegion,
            coarseX,
            0,
            coarseZ,
            COARSE_GRID_SIZE,
            COARSE_HEIGHT_SAMPLES,
            COARSE_GRID_SIZE,
            COORDINATE_SCALE,
            HEIGHT_SCALE,
            COORDINATE_SCALE
        );
        maxLimitRegion = maxLimitNoise.generateNoise(
            maxLimitRegion,
            coarseX,
            0,
            coarseZ,
            COARSE_GRID_SIZE,
            COARSE_HEIGHT_SAMPLES,
            COARSE_GRID_SIZE,
            COORDINATE_SCALE,
            HEIGHT_SCALE,
            COORDINATE_SCALE
        );

        int noiseIndex = 0;
        int depthIndex = 0;

        for (int gridX = 0; gridX < COARSE_GRID_SIZE; gridX++) {
            for (int gridZ = 0; gridZ < COARSE_GRID_SIZE; gridZ++) {
                BiomeBlend blend = blendBiomes(gridX, gridZ, biomesForGeneration);
                double depthNoiseValue = transformDepthNoise(depthRegion[depthIndex++]);
                double baseHeight = blend.baseHeight + depthNoiseValue * 0.35D;
                double heightVariation = blend.heightVariation;
                baseHeight = baseHeight * BASE_SIZE / 8.0D;
                double centerHeight = BASE_SIZE + baseHeight * 4.0D;

                for (int sampleY = 0; sampleY < COARSE_HEIGHT_SAMPLES; sampleY++) {
                    double biomeOffset = ((sampleY - centerHeight) * STRETCH_Y * 128.0D / CHUNK_HEIGHT) / heightVariation;
                    if (biomeOffset < 0.0D) {
                        biomeOffset *= 4.0D;
                    }

                    double minDensity = minLimitRegion[noiseIndex] / LOWER_LIMIT_SCALE;
                    double maxDensity = maxLimitRegion[noiseIndex] / UPPER_LIMIT_SCALE;
                    double mainDensity = clamp01((mainNoiseRegion[noiseIndex] / 10.0D + 1.0D) / 2.0D);
                    double density = lerp(minDensity, maxDensity, mainDensity) - biomeOffset;

                    if (sampleY > 29) {
                        double topFade = (sampleY - 29) / 3.0D;
                        density = density * (1.0D - topFade) + -10.0D * topFade;
                    }

                    heightMap[noiseIndex] = density;
                    noiseIndex++;
                }
            }
        }
    }

    private BiomeBlend blendBiomes(int gridX, int gridZ, Biome[] biomesForGeneration) {
        float heightVariation = 0.0F;
        float baseHeight = 0.0F;
        float weightSum = 0.0F;
        Biome center = biomesForGeneration[gridX + 2 + (gridZ + 2) * BIOME_GENERATION_GRID_SIZE];

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Biome nearby = biomesForGeneration[gridX + dx + 2 + (gridZ + dz + 2) * BIOME_GENERATION_GRID_SIZE];
                float nearbyBase = (float) (BIOME_DEPTH_OFFSET + nearby.baseHeight * BIOME_DEPTH_WEIGHT);
                float nearbyScale = (float) (BIOME_SCALE_OFFSET + nearby.heightVariation * BIOME_SCALE_WEIGHT);
                float weight = biomeWeights[dx + 2 + (dz + 2) * 5] / (nearbyBase + 2.0F);

                if (nearby.baseHeight > center.baseHeight) {
                    weight /= 2.0F;
                }

                heightVariation += nearbyScale * weight;
                baseHeight += nearbyBase * weight;
                weightSum += weight;
            }
        }

        heightVariation = heightVariation / weightSum;
        baseHeight = baseHeight / weightSum;
        heightVariation = heightVariation * 0.9F + 0.1F;
        baseHeight = (baseHeight * 4.0F - 1.0F) / 8.0F;
        return new BiomeBlend(baseHeight, Math.max(0.1F, heightVariation));
    }

    private Biome[] sampleBiomesForGeneration(int chunkX, int chunkZ) {
        Biome[] biomes = new Biome[BIOME_GENERATION_GRID_SIZE * BIOME_GENERATION_GRID_SIZE];
        int coarseStartX = chunkX * COARSE_HORIZONTAL_STEP - 2;
        int coarseStartZ = chunkZ * COARSE_HORIZONTAL_STEP - 2;

        for (int gridX = 0; gridX < BIOME_GENERATION_GRID_SIZE; gridX++) {
            for (int gridZ = 0; gridZ < BIOME_GENERATION_GRID_SIZE; gridZ++) {
                int worldX = (coarseStartX + gridX) * COARSE_HORIZONTAL_STEP;
                int worldZ = (coarseStartZ + gridZ) * COARSE_HORIZONTAL_STEP;
                biomes[gridX + gridZ * BIOME_GENERATION_GRID_SIZE] = biomeGenerator.getBiomeAt(worldX, worldZ);
            }
        }

        return biomes;
    }

    private double transformDepthNoise(double rawDepthNoise) {
        double depth = rawDepthNoise / 8000.0D;
        if (depth < 0.0D) {
            depth = -depth * 0.3D;
        }

        depth = depth * 3.0D - 2.0D;
        if (depth < 0.0D) {
            depth /= 2.0D;
            depth = Math.max(depth, -1.0D);
            depth /= 1.4D;
            depth /= 2.0D;
        } else {
            depth = Math.min(depth, 1.0D);
            depth /= 8.0D;
        }

        return depth;
    }

    private int stoneExposureHeight(Biome biome) {
        return switch (biome) {
            case MOUNTAIN -> SEA_LEVEL + 22;
            case HILLS -> SEA_LEVEL + 34;
            default -> Integer.MAX_VALUE;
        };
    }

    private int bedrockLevel(int worldX, int worldZ, int y) {
        if (y == 0) {
            return 0;
        }
        return (int) Math.floor(columnRandom(worldX, worldZ, y + 37) * 5.0D);
    }

    private double columnRandom(int worldX, int worldZ, int salt) {
        long hash = seed;
        hash ^= worldX * 341873128712L;
        hash ^= worldZ * 132897987541L;
        hash ^= salt * 42317861L;
        hash = (hash ^ (hash >>> 33)) * 0xff51afd7ed558ccdL;
        hash = (hash ^ (hash >>> 33)) * 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;
        long positive = hash & Long.MAX_VALUE;
        return positive / (double) Long.MAX_VALUE;
    }

    private double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private double lerp(double start, double end, double alpha) {
        return start + (end - start) * alpha;
    }

    public long getSeed() {
        return seed;
    }

    private static final class BiomeBlend {
        private final float baseHeight;
        private final float heightVariation;

        private BiomeBlend(float baseHeight, float heightVariation) {
            this.baseHeight = baseHeight;
            this.heightVariation = heightVariation;
        }
    }
}
