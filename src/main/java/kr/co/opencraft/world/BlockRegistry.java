package kr.co.opencraft.world;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single lookup point for OpenCraft block behavior.
 */
public final class BlockRegistry {
    private final Map<Integer, BlockDefinition> definitions;
    private final BlockDefinition air;

    private BlockRegistry(Map<Integer, BlockDefinition> definitions) {
        this.definitions = Map.copyOf(definitions);
        air = this.definitions.get(BlockTypes.AIR);
        if (air == null) {
            throw new IllegalArgumentException("registry must define air");
        }
    }

    public BlockDefinition get(int blockType) {
        if (blockType < 0) {
            return air;
        }
        BlockDefinition definition = definitions.get(blockType);
        return definition != null ? definition : unknownSolidBlock(blockType);
    }

    public boolean isKnown(int blockType) {
        return definitions.containsKey(blockType);
    }

    public boolean isSolid(int blockType) {
        return get(blockType).solid();
    }

    public int getTexture(int blockType, int faceIndex) {
        return get(blockType).textureForFace(faceIndex);
    }

    public BlockRenderLayer getRenderLayer(int blockType) {
        return get(blockType).renderLayer();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static BlockDefinition unknownSolidBlock(int blockType) {
        // Unknown positive IDs remain solid so experimental blocks keep old behavior.
        return BlockDefinition.builder(blockType, "unknown_" + blockType)
            .displayName("Unknown " + blockType)
            .material(BlockMaterial.STONE)
            .textures(BlockFaceTextures.same(blockType))
            .build();
    }

    public static final class Builder {
        private final Map<Integer, BlockDefinition> definitions = new LinkedHashMap<>();

        public Builder register(BlockDefinition definition) {
            if (definition == null) {
                throw new IllegalArgumentException("definition must not be null");
            }
            BlockDefinition previous = definitions.putIfAbsent(definition.typeId(), definition);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate block type: " + definition.typeId());
            }
            return this;
        }

        public BlockRegistry build() {
            return new BlockRegistry(definitions);
        }
    }
}
