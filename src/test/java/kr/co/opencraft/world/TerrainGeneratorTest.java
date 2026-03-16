package kr.co.opencraft.world;

import kr.co.voxelite.world.Chunk;
import kr.co.voxelite.world.ChunkCoord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerrainGeneratorTest {

    @Test
    void generateTerrain_ShouldBeDeterministicForSameSeed() {
        TerrainGenerator generator = new TerrainGenerator(1234L);
        Chunk first = new Chunk(new ChunkCoord(0, 0));
        Chunk second = new Chunk(new ChunkCoord(0, 0));

        generator.generateTerrain(first, BlockTypes.GRASS);
        generator.generateTerrain(second, BlockTypes.GRASS);

        for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                assertEquals(
                    findTopSolidY(first, localX, localZ),
                    findTopSolidY(second, localX, localZ),
                    "heightmap should be deterministic for a fixed seed"
                );
            }
        }
    }

    @Test
    void generateTerrain_ShouldCreateModerateHeightVariationAcrossRegion() {
        TerrainGenerator generator = new TerrainGenerator(1234L);
        List<Integer> heights = new ArrayList<>();

        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                Chunk chunk = new Chunk(new ChunkCoord(chunkX, chunkZ));
                generator.generateTerrain(chunk, BlockTypes.GRASS);

                for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
                    for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                        heights.add(findTopSolidY(chunk, localX, localZ));
                    }
                }
            }
        }

        int minHeight = heights.stream().mapToInt(Integer::intValue).min().orElseThrow();
        int maxHeight = heights.stream().mapToInt(Integer::intValue).max().orElseThrow();
        assertTrue(maxHeight - minHeight >= 18, "terrain should still vary across nearby chunks (min=" + minHeight + ", max=" + maxHeight + ")");
        assertTrue(maxHeight <= 128, "minecraft-style baseline terrain should not explode into constant cliffs (min=" + minHeight + ", max=" + maxHeight + ")");
    }

    @Test
    void generateTerrain_ShouldApplySurfaceAndSubsurfaceLayers() {
        TerrainGenerator generator = new TerrainGenerator(1234L);
        boolean foundGrassSurface = false;
        boolean foundDirtBelowSurface = false;

        for (int chunkX = -1; chunkX <= 1 && (!foundGrassSurface || !foundDirtBelowSurface); chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1 && (!foundGrassSurface || !foundDirtBelowSurface); chunkZ++) {
                Chunk chunk = new Chunk(new ChunkCoord(chunkX, chunkZ));
                generator.generateTerrain(chunk, BlockTypes.GRASS);

                for (int localX = 0; localX < Chunk.CHUNK_SIZE; localX++) {
                    for (int localZ = 0; localZ < Chunk.CHUNK_SIZE; localZ++) {
                        int topY = findTopSolidY(chunk, localX, localZ);
                        if (topY < 2) {
                            continue;
                        }

                        Chunk.BlockData topBlock = chunk.getBlock(localX, topY, localZ);
                        Chunk.BlockData below = chunk.getBlock(localX, topY - 1, localZ);
                        if (topBlock != null && topBlock.blockType == BlockTypes.GRASS) {
                            foundGrassSurface = true;
                            if (below != null && below.blockType == BlockTypes.DIRT) {
                                foundDirtBelowSurface = true;
                            }
                        }
                    }
                }
            }
        }

        assertTrue(foundGrassSurface, "surface replacement should create grass-topped columns");
        assertTrue(foundDirtBelowSurface, "surface replacement should leave a dirt layer under at least one grass column");
    }
    private int findTopSolidY(Chunk chunk, int localX, int localZ) {
        for (int y = 255; y >= 0; y--) {
            if (chunk.getBlock(localX, y, localZ) != null) {
                return y;
            }
        }
        return -1;
    }
}
