package kr.co.opencraft.world;

import kr.co.voxelite.world.Chunk;
import kr.co.voxelite.world.IChunkGenerator;

/**
 * TerrainGenerator를 라이브러리 인터페이스에 맞게 변환
 */
public class TerrainGeneratorAdapter implements IChunkGenerator {
    private final TerrainGenerator terrainGenerator;
    
    public TerrainGeneratorAdapter(TerrainGenerator terrainGenerator) {
        this.terrainGenerator = terrainGenerator;
    }
    
    @Override
    public void generateChunk(Chunk chunk, int blockType) {
        terrainGenerator.generateTerrain(chunk, blockType);
    }
}
