package com.fandreuz.grpc.uds;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.InsecureServerCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum Transport {
    TCP_IP,
    UDS;

    public ChannelWithToken makeChannel() {
        return switch (this) {
            case TCP_IP -> {
                var port = Utils.findPort();
                var channel = Grpc.newChannelBuilderForAddress("localhost", port, InsecureChannelCredentials.create())
                        .maxInboundMessageSize(Server.GRPC_MESSAGE_SIZE_LIMIT_BYTES_INT)
                        .build();
                yield new ChannelWithToken(channel, port);
            }
            case UDS -> {
                var socket = Path.of(System.getProperty("java.io.tmpdir"))
                        .resolve(UUID.randomUUID().toString());

                var channel = NettyChannelBuilder.forAddress(
                                new DomainSocketAddress(socket.toFile()), InsecureChannelCredentials.create())
                        .eventLoopGroup(new EpollEventLoopGroup())
                        .channelType(EpollDomainSocketChannel.class)
                        .build();
                yield new ChannelWithToken(channel, socket);
            }
        };
    }

    public ServerBuilder<?> makeServerBuilder(String serializedToken) {
        return switch (this) {
            case TCP_IP -> {
                var port = Integer.parseInt(serializedToken);
                yield Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create());
            }
            case UDS -> {
                var executor = new ThreadPoolExecutor(2, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000));
                yield NettyServerBuilder.forAddress(
                                new DomainSocketAddress(Path.of(serializedToken).toFile()))
                        .bossEventLoopGroup(
                                new EpollEventLoopGroup(5, MoreExecutors.getExitingExecutorService(executor)))
                        .workerEventLoopGroup(
                                new EpollEventLoopGroup(5, MoreExecutors.getExitingExecutorService(executor)))
                        .channelType(EpollServerDomainSocketChannel.class);
            }
        };
    }

    public record ChannelWithToken(ManagedChannel channel, Object token) {}
}
