package kr.co.opencraft.world.noise;

import java.util.Random;

/**
 * Octave wrapper that mirrors Minecraft's array-based noise accumulation.
 */
public final class OctaveNoise {
    private final ImprovedNoise[] octaves;

    public OctaveNoise(Random random, int octaveCount) {
        this.octaves = new ImprovedNoise[octaveCount];
        for (int i = 0; i < octaveCount; i++) {
            octaves[i] = new ImprovedNoise(random);
        }
    }

    public double[] generateNoise(
        double[] noiseArray,
        int xOffset,
        int yOffset,
        int zOffset,
        int xSize,
        int ySize,
        int zSize,
        double xScale,
        double yScale,
        double zScale
    ) {
        int size = xSize * ySize * zSize;
        if (noiseArray == null || noiseArray.length != size) {
            noiseArray = new double[size];
        } else {
            for (int i = 0; i < noiseArray.length; i++) {
                noiseArray[i] = 0.0D;
            }
        }

        double octaveScale = 1.0D;
        for (ImprovedNoise octave : octaves) {
            double sampleX = xOffset * octaveScale * xScale;
            double sampleY = yOffset * octaveScale * yScale;
            double sampleZ = zOffset * octaveScale * zScale;
            long wrappedX = floor(sampleX);
            long wrappedZ = floor(sampleZ);
            sampleX -= wrappedX;
            sampleZ -= wrappedZ;
            wrappedX %= 16777216L;
            wrappedZ %= 16777216L;
            sampleX += wrappedX;
            sampleZ += wrappedZ;

            octave.populateNoiseArray(
                noiseArray,
                sampleX,
                sampleY,
                sampleZ,
                xSize,
                ySize,
                zSize,
                xScale * octaveScale,
                yScale * octaveScale,
                zScale * octaveScale,
                octaveScale
            );
            octaveScale /= 2.0D;
        }

        return noiseArray;
    }

    public double[] generateNoise(
        double[] noiseArray,
        int xOffset,
        int zOffset,
        int xSize,
        int zSize,
        double xScale,
        double zScale,
        double ignored
    ) {
        return generateNoise(noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1.0D, zScale);
    }

    private long floor(double value) {
        long truncated = (long) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
