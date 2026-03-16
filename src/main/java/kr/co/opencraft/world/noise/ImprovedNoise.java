package kr.co.opencraft.world.noise;

import java.util.Random;

/**
 * Adapted improved Perlin noise implementation used to mirror Minecraft's octave pipeline.
 */
final class ImprovedNoise {
    private static final double[] GRAD_X = {1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D};
    private static final double[] GRAD_Y = {1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D};
    private static final double[] GRAD_Z = {0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D};
    private static final double[] GRAD_2X = {1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D};
    private static final double[] GRAD_2Z = {0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D};

    private final int[] permutations;
    private final double xCoord;
    private final double yCoord;
    private final double zCoord;

    ImprovedNoise(Random random) {
        this.permutations = new int[512];
        this.xCoord = random.nextDouble() * 256.0D;
        this.yCoord = random.nextDouble() * 256.0D;
        this.zCoord = random.nextDouble() * 256.0D;

        for (int i = 0; i < 256; i++) {
            permutations[i] = i;
        }

        for (int i = 0; i < 256; i++) {
            int swapIndex = random.nextInt(256 - i) + i;
            int value = permutations[i];
            permutations[i] = permutations[swapIndex];
            permutations[swapIndex] = value;
            permutations[i + 256] = permutations[i];
        }
    }

    void populateNoiseArray(
        double[] noiseArray,
        double xOffset,
        double yOffset,
        double zOffset,
        int xSize,
        int ySize,
        int zSize,
        double xScale,
        double yScale,
        double zScale,
        double noiseScale
    ) {
        if (ySize == 1) {
            populate2D(noiseArray, xOffset, zOffset, xSize, zSize, xScale, zScale, noiseScale);
            return;
        }

        int index = 0;
        double scaleFactor = 1.0D / noiseScale;
        int cachedY = -1;
        int permL = 0;
        int permI1 = 0;
        int permJ1 = 0;
        int permK1 = 0;
        int permL1 = 0;
        int permI2 = 0;
        double grad1 = 0.0D;
        double grad2 = 0.0D;
        double grad3 = 0.0D;
        double grad4 = 0.0D;

        for (int x = 0; x < xSize; x++) {
            double sampleX = xOffset + x * xScale + xCoord;
            int sampleXFloor = floor(sampleX);
            int permX = sampleXFloor & 255;
            sampleX -= sampleXFloor;
            double fadeX = fade(sampleX);

            for (int z = 0; z < zSize; z++) {
                double sampleZ = zOffset + z * zScale + zCoord;
                int sampleZFloor = floor(sampleZ);
                int permZ = sampleZFloor & 255;
                sampleZ -= sampleZFloor;
                double fadeZ = fade(sampleZ);

                for (int y = 0; y < ySize; y++) {
                    double sampleY = yOffset + y * yScale + yCoord;
                    int sampleYFloor = floor(sampleY);
                    int permY = sampleYFloor & 255;
                    sampleY -= sampleYFloor;
                    double fadeY = fade(sampleY);

                    if (y == 0 || permY != cachedY) {
                        cachedY = permY;
                        permL = permutations[permX] + permY;
                        permI1 = permutations[permL] + permZ;
                        permJ1 = permutations[permL + 1] + permZ;
                        permK1 = permutations[permX + 1] + permY;
                        permL1 = permutations[permK1] + permZ;
                        permI2 = permutations[permK1 + 1] + permZ;
                        grad1 = lerp(fadeX, grad(permutations[permI1], sampleX, sampleY, sampleZ), grad(permutations[permL1], sampleX - 1.0D, sampleY, sampleZ));
                        grad2 = lerp(fadeX, grad(permutations[permJ1], sampleX, sampleY - 1.0D, sampleZ), grad(permutations[permI2], sampleX - 1.0D, sampleY - 1.0D, sampleZ));
                        grad3 = lerp(fadeX, grad(permutations[permI1 + 1], sampleX, sampleY, sampleZ - 1.0D), grad(permutations[permL1 + 1], sampleX - 1.0D, sampleY, sampleZ - 1.0D));
                        grad4 = lerp(fadeX, grad(permutations[permJ1 + 1], sampleX, sampleY - 1.0D, sampleZ - 1.0D), grad(permutations[permI2 + 1], sampleX - 1.0D, sampleY - 1.0D, sampleZ - 1.0D));
                    }

                    double lower = lerp(fadeY, grad1, grad2);
                    double upper = lerp(fadeY, grad3, grad4);
                    noiseArray[index++] += lerp(fadeZ, lower, upper) * scaleFactor;
                }
            }
        }
    }

    private void populate2D(
        double[] noiseArray,
        double xOffset,
        double zOffset,
        int xSize,
        int zSize,
        double xScale,
        double zScale,
        double noiseScale
    ) {
        int index = 0;
        double scaleFactor = 1.0D / noiseScale;

        for (int x = 0; x < xSize; x++) {
            double sampleX = xOffset + x * xScale + xCoord;
            int sampleXFloor = floor(sampleX);
            int permX = sampleXFloor & 255;
            sampleX -= sampleXFloor;
            double fadeX = fade(sampleX);

            for (int z = 0; z < zSize; z++) {
                double sampleZ = zOffset + z * zScale + zCoord;
                int sampleZFloor = floor(sampleZ);
                int permZ = sampleZFloor & 255;
                sampleZ -= sampleZFloor;
                double fadeZ = fade(sampleZ);

                int permI = permutations[permX];
                int permJ = permutations[permI] + permZ;
                int permK = permutations[permX + 1];
                int permL = permutations[permK] + permZ;

                double lower = lerp(fadeX, grad2(permutations[permJ], sampleX, sampleZ), grad(permutations[permL], sampleX - 1.0D, 0.0D, sampleZ));
                double upper = lerp(fadeX, grad(permutations[permJ + 1], sampleX, 0.0D, sampleZ - 1.0D), grad(permutations[permL + 1], sampleX - 1.0D, 0.0D, sampleZ - 1.0D));
                noiseArray[index++] += lerp(fadeZ, lower, upper) * scaleFactor;
            }
        }
    }

    private double grad2(int hash, double x, double z) {
        int index = hash & 15;
        return GRAD_2X[index] * x + GRAD_2Z[index] * z;
    }

    private double grad(int hash, double x, double y, double z) {
        int index = hash & 15;
        return GRAD_X[index] * x + GRAD_Y[index] * y + GRAD_Z[index] * z;
    }

    private double lerp(double alpha, double start, double end) {
        return start + alpha * (end - start);
    }

    private double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private int floor(double value) {
        int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
