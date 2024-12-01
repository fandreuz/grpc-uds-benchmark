package com.fandreuz.grpc.uds;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerUtils {

    public ServerBuilder<?> makeServerBuilder(Transport transport, String serializedToken) {
        return switch (transport) {
            case TCP_IP -> {
                var port = Integer.parseInt(serializedToken);
                yield Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create());
            }
            case UDS -> {
                var bossThreadFactory = new ThreadFactoryBuilder()
                        .setNameFormat("boss-pool-%d")
                        .setDaemon(true)
                        .build();
                var bossExecutor = new ThreadPoolExecutor(
                        2, 5, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), bossThreadFactory);

                var workThreadFactory = new ThreadFactoryBuilder()
                        .setNameFormat("work-pool-%d")
                        .setDaemon(true)
                        .build();
                var workExecutor = new ThreadPoolExecutor(
                        2, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), workThreadFactory);

                yield NettyServerBuilder.forAddress(
                                new DomainSocketAddress(Path.of(serializedToken).toFile()))
                        .bossEventLoopGroup(new EpollEventLoopGroup(5, bossExecutor))
                        .workerEventLoopGroup(new EpollEventLoopGroup(10, workExecutor))
                        .channelType(EpollServerDomainSocketChannel.class);
            }
        };
    }
}
