package kr.co.opencraft.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkLoadPolicyRenderDistanceTest {

    @Test
    void minecraftStyleRenderDistance_ShouldUseFullChunksThroughSixteenChunks() {
        ChunkLoadPolicy policy = new ChunkLoadPolicy(16, 17, 18);

        assertEquals(16, policy.getVisibleDistance());
        assertEquals(17, policy.getKeepLoadedDistance());
        assertEquals(18, policy.getPregenerateDistance());
        assertTrue(policy.shouldLoadToMemory(16, 0, 0, 0));
        assertFalse(policy.shouldLoadToMemory(17, 0, 0, 0));
    }
}
