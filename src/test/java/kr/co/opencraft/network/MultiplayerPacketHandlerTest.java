package kr.co.opencraft.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.badlogic.gdx.math.Vector3;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.co.voxeliver.network.protocol.Packet;
import kr.co.voxeliver.network.protocol.impl.ChunkUnloadPacket;
import kr.co.voxeliver.network.protocol.impl.LoginAcceptedPacket;
import kr.co.voxelite.world.ChunkCoord;
import org.junit.jupiter.api.Test;

class MultiplayerPacketHandlerTest {

    @Test
    void completesLoginFutureWhenAcceptancePacketArrives() {
        CompletableFuture<LoginAcceptedPacket> loginFuture = new CompletableFuture<>();
        ConcurrentLinkedQueue<Packet> inboundPackets = new ConcurrentLinkedQueue<>();
        EmbeddedChannel channel = new EmbeddedChannel(new MultiplayerPacketHandler(loginFuture, inboundPackets));

        try {
            LoginAcceptedPacket packet = new LoginAcceptedPacket(17, new Vector3(1f, 2f, 3f));

            channel.writeInbound(packet);

            LoginAcceptedPacket accepted = loginFuture.join();
            assertNotNull(accepted);
            assertEquals(17, accepted.getPlayerId());
            assertEquals(new Vector3(1f, 2f, 3f), accepted.getSpawnPosition());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void queuesStatePacketsForGameScreen() {
        CompletableFuture<LoginAcceptedPacket> loginFuture = new CompletableFuture<>();
        ConcurrentLinkedQueue<Packet> inboundPackets = new ConcurrentLinkedQueue<>();
        EmbeddedChannel channel = new EmbeddedChannel(new MultiplayerPacketHandler(loginFuture, inboundPackets));

        try {
            ChunkUnloadPacket packet = new ChunkUnloadPacket(new ChunkCoord(3, 4));

            channel.writeInbound(packet);

            assertSame(packet, inboundPackets.poll());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
