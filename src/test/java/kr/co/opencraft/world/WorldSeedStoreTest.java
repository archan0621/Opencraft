package kr.co.opencraft.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldSeedStoreTest {

    @TempDir
    Path worldPath;

    @Test
    void loadOrCreateSeed_ShouldCreateAndReuseSeedMetadata() throws IOException {
        long firstSeed = WorldSeedStore.loadOrCreateSeed(worldPath, () -> 4242L);
        long secondSeed = WorldSeedStore.loadOrCreateSeed(worldPath, () -> 9999L);

        assertEquals(4242L, firstSeed);
        assertEquals(4242L, secondSeed);
        assertTrue(Files.exists(worldPath.resolve("world.properties")));
    }

    @Test
    void loadOrCreateSeed_ShouldMoveLegacyChunksWhenSeedMetadataIsMissing() throws IOException {
        Path chunksPath = worldPath.resolve("chunks");
        Files.createDirectories(chunksPath);
        Files.writeString(chunksPath.resolve("chunk_0_0.dat"), "legacy");

        long seed = WorldSeedStore.loadOrCreateSeed(worldPath, () -> 1234L);

        assertEquals(1234L, seed);
        assertFalse(Files.exists(chunksPath));
        assertTrue(hasLegacyChunksBackup());
    }

    @Test
    void loadOrCreateSeed_ShouldRejectInvalidSeedMetadata() throws IOException {
        Files.writeString(worldPath.resolve("world.properties"), "seed=not-a-number\n");

        assertThrows(IOException.class, () -> WorldSeedStore.loadOrCreateSeed(worldPath, () -> 1234L));
    }

    private boolean hasLegacyChunksBackup() throws IOException {
        try (Stream<Path> paths = Files.list(worldPath)) {
            return paths.anyMatch(path -> path.getFileName().toString().startsWith("chunks_legacy_"));
        }
    }
}
