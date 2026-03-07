package kr.co.opencraft.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import kr.co.voxeliver.network.protocol.Packet;
import kr.co.voxeliver.network.protocol.impl.LoginAcceptedPacket;
import kr.co.voxeliver.network.protocol.impl.LoginRequestPacket;
import kr.co.voxeliver.network.protocol.impl.PingPacket;

public class MultiplayerClient implements AutoCloseable {
    private static final float KEEP_ALIVE_INTERVAL_SECONDS = 5f;

    private final String host;
    private final int port;
    private final CompletableFuture<LoginAcceptedPacket> loginFuture = new CompletableFuture<>();
    private final ConcurrentLinkedQueue<Packet> inboundPackets = new ConcurrentLinkedQueue<>();

    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private float keepAliveAccumulator;

    public MultiplayerClient(String host, int port) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
    }

    public LoginAcceptedPacket connectAndLogin(String username, Duration timeout) throws Exception {
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (channel != null && channel.isActive()) {
            throw new IllegalStateException("Client is already connected");
        }

        eventLoopGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new MultiplayerClientInitializer(loginFuture, inboundPackets));

        channel = bootstrap.connect(host, port).sync().channel();
        channel.writeAndFlush(new LoginRequestPacket(username)).sync();
        return loginFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void update(float delta) {
        if (!Float.isFinite(delta) || delta <= 0f || !isConnected()) {
            return;
        }

        keepAliveAccumulator += delta;
        if (keepAliveAccumulator >= KEEP_ALIVE_INTERVAL_SECONDS) {
            send(new PingPacket());
            keepAliveAccumulator = 0f;
        }
    }

    public void send(Packet packet) {
        if (packet == null || !isConnected()) {
            return;
        }
        channel.writeAndFlush(packet);
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public Packet pollPacket() {
        return inboundPackets.poll();
    }

    @Override
    public void close() {
        keepAliveAccumulator = 0f;
        inboundPackets.clear();

        if (channel != null) {
            channel.close().awaitUninterruptibly();
            channel = null;
        }

        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully().awaitUninterruptibly();
            eventLoopGroup = null;
        }
    }
}
