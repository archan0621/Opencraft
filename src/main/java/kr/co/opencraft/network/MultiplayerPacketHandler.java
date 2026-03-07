package kr.co.opencraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.co.voxeliver.network.protocol.Packet;
import kr.co.voxeliver.network.protocol.impl.BlockUpdatePacket;
import kr.co.voxeliver.network.protocol.impl.ChunkDataPacket;
import kr.co.voxeliver.network.protocol.impl.ChunkUnloadPacket;
import kr.co.voxeliver.network.protocol.impl.LoginAcceptedPacket;
import kr.co.voxeliver.network.protocol.impl.PlayerJoinedPacket;
import kr.co.voxeliver.network.protocol.impl.PlayerLeftPacket;
import kr.co.voxeliver.network.protocol.impl.PlayerStatePacket;
import kr.co.voxeliver.network.protocol.impl.PingPacket;

public class MultiplayerPacketHandler extends SimpleChannelInboundHandler<Packet> {
    private final CompletableFuture<LoginAcceptedPacket> loginFuture;
    private final ConcurrentLinkedQueue<Packet> inboundPackets;

    public MultiplayerPacketHandler(CompletableFuture<LoginAcceptedPacket> loginFuture, ConcurrentLinkedQueue<Packet> inboundPackets) {
        this.loginFuture = loginFuture;
        this.inboundPackets = inboundPackets;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof LoginAcceptedPacket loginAcceptedPacket) {
            loginFuture.complete(loginAcceptedPacket);
            return;
        }

        if (packet instanceof PingPacket) {
            return;
        }

        if (packet instanceof ChunkDataPacket
            || packet instanceof ChunkUnloadPacket
            || packet instanceof BlockUpdatePacket
            || packet instanceof PlayerJoinedPacket
            || packet instanceof PlayerStatePacket
            || packet instanceof PlayerLeftPacket) {
            inboundPackets.offer(packet);
            return;
        }

        System.out.println("[MultiplayerClient] Unhandled packet: " + packet.getClass().getSimpleName());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (!loginFuture.isDone()) {
            loginFuture.completeExceptionally(new IllegalStateException("Disconnected before login completed"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!loginFuture.isDone()) {
            loginFuture.completeExceptionally(cause);
        }
        System.err.println("[MultiplayerClient] " + cause.getMessage());
        ctx.close();
    }
}
