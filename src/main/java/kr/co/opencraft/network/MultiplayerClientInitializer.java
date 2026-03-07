package kr.co.opencraft.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.co.voxeliver.network.protocol.Packet;
import kr.co.voxeliver.network.codec.PacketDecoder;
import kr.co.voxeliver.network.codec.PacketEncoder;
import kr.co.voxeliver.network.protocol.impl.LoginAcceptedPacket;

public class MultiplayerClientInitializer extends ChannelInitializer<SocketChannel> {
    private final CompletableFuture<LoginAcceptedPacket> loginFuture;
    private final ConcurrentLinkedQueue<Packet> inboundPackets;

    public MultiplayerClientInitializer(CompletableFuture<LoginAcceptedPacket> loginFuture, ConcurrentLinkedQueue<Packet> inboundPackets) {
        this.loginFuture = loginFuture;
        this.inboundPackets = inboundPackets;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(new PacketDecoder())
            .addLast(new MultiplayerPacketHandler(loginFuture, inboundPackets))
            .addLast(new PacketEncoder());
    }
}
