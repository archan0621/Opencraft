package kr.co.opencraft.world;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

public final class WorldSeedStore {
    private static final String METADATA_FILE = "world.properties";
    private static final String SEED_KEY = "seed";
    private static final String LEGACY_CHUNKS_PREFIX = "chunks_legacy_";

    private WorldSeedStore() {
    }

    public static long loadOrCreateSeed(String worldPath) throws IOException {
        return loadOrCreateSeed(Path.of(worldPath), System::currentTimeMillis);
    }

    static long loadOrCreateSeed(Path worldPath, LongSupplier seedSupplier) throws IOException {
        Files.createDirectories(worldPath);

        Path metadataPath = worldPath.resolve(METADATA_FILE);
        if (Files.exists(metadataPath)) {
            return readSeed(metadataPath);
        }

        protectLegacyChunksWithoutSeed(worldPath);
        long seed = seedSupplier.getAsLong();
        writeSeed(metadataPath, seed);
        return seed;
    }

    private static long readSeed(Path metadataPath) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(metadataPath)) {
            properties.load(input);
        }

        String seedValue = properties.getProperty(SEED_KEY);
        if (seedValue == null || seedValue.isBlank()) {
            throw new IOException("World metadata is missing seed: " + metadataPath);
        }

        try {
            return Long.parseLong(seedValue.trim());
        } catch (NumberFormatException e) {
            throw new IOException("World metadata has invalid seed: " + metadataPath, e);
        }
    }

    private static void writeSeed(Path metadataPath, long seed) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(SEED_KEY, Long.toString(seed));
        try (OutputStream output = Files.newOutputStream(metadataPath)) {
            properties.store(output, "OpenCraft world metadata");
        }
    }

    private static void protectLegacyChunksWithoutSeed(Path worldPath) throws IOException {
        Path chunksPath = worldPath.resolve("chunks");
        if (!containsChunkFiles(chunksPath)) {
            return;
        }

        Path backupPath = nextLegacyChunksPath(worldPath);
        Files.move(chunksPath, backupPath);
        System.err.println("[WorldSeedStore] Moved chunks without seed metadata to " + backupPath);
    }

    private static boolean containsChunkFiles(Path chunksPath) throws IOException {
        if (!Files.isDirectory(chunksPath)) {
            return false;
        }

        try (Stream<Path> paths = Files.list(chunksPath)) {
            return paths.anyMatch(path -> {
                String fileName = path.getFileName().toString();
                return Files.isRegularFile(path)
                    && fileName.startsWith("chunk_")
                    && fileName.endsWith(".dat");
            });
        }
    }

    private static Path nextLegacyChunksPath(Path worldPath) {
        long timestamp = System.currentTimeMillis();
        Path candidate = worldPath.resolve(LEGACY_CHUNKS_PREFIX + timestamp);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = worldPath.resolve(LEGACY_CHUNKS_PREFIX + timestamp + "_" + suffix);
            suffix++;
        }
        return candidate;
    }
}
