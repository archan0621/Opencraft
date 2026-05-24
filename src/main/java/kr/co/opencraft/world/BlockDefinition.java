package kr.co.opencraft.world;

import java.util.Locale;
import java.util.Objects;
import kr.co.voxelite.world.BlockRenderLayer;

/**
 * Immutable behavior profile for one block type.
 */
public record BlockDefinition(
    int typeId,
    String key,
    String displayName,
    BlockMaterial material,
    BlockFaceTextures textures,
    boolean solid,
    BlockRenderLayer renderLayer,
    float hardness,
    int lightOpacity
) {
    public BlockDefinition {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        key = key.toLowerCase(Locale.ROOT);
        displayName = displayName == null || displayName.isBlank() ? key : displayName;
        material = Objects.requireNonNull(material, "material");
        textures = Objects.requireNonNull(textures, "textures");
        renderLayer = Objects.requireNonNull(renderLayer, "renderLayer");
        if (!Float.isFinite(hardness) || hardness < 0f) {
            throw new IllegalArgumentException("hardness must be a non-negative finite number");
        }
        if (lightOpacity < 0 || lightOpacity > 255) {
            throw new IllegalArgumentException("lightOpacity must be between 0 and 255");
        }
    }

    public int textureForFace(int faceIndex) {
        return textures.textureForFace(faceIndex);
    }

    public static Builder builder(int typeId, String key) {
        return new Builder(typeId, key);
    }

    public static final class Builder {
        private final int typeId;
        private final String key;
        private String displayName;
        private BlockMaterial material = BlockMaterial.STONE;
        private BlockFaceTextures textures;
        private boolean solid = true;
        private BlockRenderLayer renderLayer = BlockRenderLayer.SOLID;
        private float hardness = 1f;
        private int lightOpacity = 255;

        private Builder(int typeId, String key) {
            this.typeId = typeId;
            this.key = key;
            this.textures = BlockFaceTextures.same(Math.max(typeId, 0));
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder material(BlockMaterial material) {
            this.material = material;
            return this;
        }

        public Builder textures(BlockFaceTextures textures) {
            this.textures = textures;
            return this;
        }

        public Builder solid(boolean solid) {
            this.solid = solid;
            return this;
        }

        public Builder renderLayer(BlockRenderLayer renderLayer) {
            this.renderLayer = renderLayer;
            return this;
        }

        public Builder hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Builder lightOpacity(int lightOpacity) {
            this.lightOpacity = lightOpacity;
            return this;
        }

        public BlockDefinition build() {
            return new BlockDefinition(
                typeId,
                key,
                displayName,
                material,
                textures,
                solid,
                renderLayer,
                hardness,
                lightOpacity
            );
        }
    }
}
